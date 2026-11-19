package com.stocksim.trading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.stocksim.IntegrationTest;
import com.stocksim.account.AccountService;
import com.stocksim.common.ApiException;
import com.stocksim.portfolio.PortfolioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

class TradingIntegrationTest extends IntegrationTest {

	@Autowired
	TradingService trading;

	@Autowired
	PortfolioService portfolio;

	@Autowired
	AccountService accounts;

	@Autowired
	JdbcClient jdbc;

	long userId;

	@BeforeEach
	void newUser() {
		userId = jdbc.sql("INSERT INTO users (email, display_name, password_hash) VALUES (?, 'T', 'x') RETURNING id")
			.param(UUID.randomUUID() + "@test.local")
			.query(Long.class)
			.single();
		accounts.openAccount(userId);
		fakeMarket.pinPrice("AAPL", new BigDecimal("200.0000"));
	}

	private OrderView order(OrderSide side, String symbol, int qty) {
		return trading.placeMarketOrder(userId, symbol, side, BigDecimal.valueOf(qty), UUID.randomUUID().toString());
	}

	@Test
	void buyThenSellUpdatesCashPositionAndRealizedPnl() {
		OrderView buy = order(OrderSide.BUY, "AAPL", 10);
		assertThat(buy.status()).isEqualTo("FILLED");
		assertThat(buy.fill().grossAmount()).isEqualByComparingTo("2000.00");

		fakeMarket.pinPrice("AAPL", new BigDecimal("250.0000"));
		redis.delete("quote:AAPL");
		OrderView sell = order(OrderSide.SELL, "AAPL", 4);
		assertThat(sell.status()).isEqualTo("FILLED");
		assertThat(sell.fill().realizedPnl()).isEqualByComparingTo("200.00"); // 4 * (250 - 200)

		var p = portfolio.get(userId);
		assertThat(p.cash()).isEqualByComparingTo("99000.00"); // 100000 - 2000 + 1000
		assertThat(p.holdings()).singleElement().satisfies(h -> {
			assertThat(h.qty()).isEqualByComparingTo("6");
			assertThat(h.costBasis()).isEqualByComparingTo("1200.00");
			assertThat(h.marketValue()).isEqualByComparingTo("1500.00");
			assertThat(h.unrealizedPnl()).isEqualByComparingTo("300.00");
		});
		assertThat(p.realizedPnl()).isEqualByComparingTo("200.00");
		assertThat(p.equity()).isEqualByComparingTo("100500.00");
		assertThat(p.totalReturn()).isEqualByComparingTo("500.00");
		assertLedgerReconciles();
	}

	@Test
	void sellingAllSharesClosesThePosition() {
		order(OrderSide.BUY, "AAPL", 3);
		order(OrderSide.SELL, "AAPL", 3);
		assertThat(portfolio.get(userId).holdings()).isEmpty();
		assertLedgerReconciles();
	}

	@Test
	void rejectsWhenCashIsNotEnough() {
		OrderView o = order(OrderSide.BUY, "AAPL", 501); // 100200 > 100000
		assertThat(o.status()).isEqualTo("REJECTED");
		assertThat(o.rejectReason()).isEqualTo(RejectReason.INSUFFICIENT_FUNDS);
		assertThat(portfolio.get(userId).cash()).isEqualByComparingTo("100000.00");
	}

	@Test
	void rejectsSellingMoreThanHeld() {
		order(OrderSide.BUY, "AAPL", 2);
		OrderView o = order(OrderSide.SELL, "AAPL", 3);
		assertThat(o.rejectReason()).isEqualTo(RejectReason.INSUFFICIENT_SHARES);
	}

	@Test
	void rejectsWhenMarketIsClosed() {
		fakeMarket.setMarketOpen(false);
		OrderView o = order(OrderSide.BUY, "AAPL", 1);
		assertThat(o.status()).isEqualTo("REJECTED");
		assertThat(o.rejectReason()).isEqualTo(RejectReason.MARKET_CLOSED);
		assertThat(portfolio.get(userId).holdings()).isEmpty();
	}

	@Test
	void rejectsInvalidInput() {
		assertThatThrownBy(() -> order(OrderSide.BUY, "NOPE", 1)).isInstanceOf(ApiException.class);
		assertThatThrownBy(() -> order(OrderSide.BUY, "AAPL", 0)).isInstanceOf(ApiException.class);
		assertThatThrownBy(() -> trading.placeMarketOrder(userId, "AAPL", OrderSide.BUY, new BigDecimal("1.5"), "k"))
			.isInstanceOf(ApiException.class);
	}

	@Test
	void sameIdempotencyKeyReturnsFirstResult() {
		OrderView first = trading.placeMarketOrder(userId, "AAPL", OrderSide.BUY, BigDecimal.ONE, "key-1");
		OrderView again = trading.placeMarketOrder(userId, "AAPL", OrderSide.BUY, BigDecimal.ONE, "key-1");
		assertThat(again).isEqualTo(first);
		assertThat(portfolio.get(userId).holdings()).singleElement()
			.satisfies(h -> assertThat(h.qty()).isEqualByComparingTo("1"));

		assertThatThrownBy(() -> trading.placeMarketOrder(userId, "AAPL", OrderSide.BUY, BigDecimal.TWO, "key-1"))
			.isInstanceOf(ApiException.class)
			.hasMessageContaining("Idempotency-Key");
	}

	@Test
	void parallelBuysNeverOverspend() throws Exception {
		// 20 parallel orders of 50 shares at 200 = 10000 each; only 10 fit into 100000.
		List<Callable<OrderView>> tasks = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			tasks.add(() -> order(OrderSide.BUY, "AAPL", 50));
		}
		List<OrderView> results = runAll(tasks);

		assertThat(results).filteredOn(o -> o.status().equals("FILLED")).hasSize(10);
		assertThat(results).filteredOn(o -> o.rejectReason() == RejectReason.INSUFFICIENT_FUNDS).hasSize(10);
		assertThat(portfolio.get(userId).cash()).isEqualByComparingTo("0.00");
		assertLedgerReconciles();
	}

	@Test
	void parallelRetriesWithSameKeyCreateOneOrder() throws Exception {
		List<Callable<OrderView>> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			tasks.add(() -> trading.placeMarketOrder(userId, "AAPL", OrderSide.BUY, BigDecimal.ONE, "same-key"));
		}
		List<OrderView> results = runAll(tasks);

		assertThat(results).extracting(OrderView::id).containsOnly(results.getFirst().id());
		assertThat(portfolio.get(userId).holdings()).singleElement()
			.satisfies(h -> assertThat(h.qty()).isEqualByComparingTo("1"));
		assertLedgerReconciles();
	}

	private static List<OrderView> runAll(List<Callable<OrderView>> tasks) throws Exception {
		try (ExecutorService pool = Executors.newFixedThreadPool(tasks.size())) {
			List<OrderView> out = new ArrayList<>();
			for (Future<OrderView> f : pool.invokeAll(tasks)) {
				out.add(f.get());
			}
			return out;
		}
	}

	/** Core rule: cached cash and positions must equal what the ledger says. */
	private void assertLedgerReconciles() {
		long accountId = accounts.forUser(userId).id();
		BigDecimal ledgerCash = jdbc.sql("SELECT SUM(cash_amount) FROM ledger_entries WHERE account_id = ?")
			.param(accountId)
			.query(BigDecimal.class)
			.single();
		assertThat(accounts.forUser(userId).cashBalance()).isEqualByComparingTo(ledgerCash);

		Map<String, BigDecimal> ledgerQty = new java.util.HashMap<>();
		jdbc.sql("""
				SELECT symbol, SUM(qty_amount) FROM ledger_entries
				WHERE account_id = ? AND symbol IS NOT NULL GROUP BY symbol HAVING SUM(qty_amount) <> 0
				""").param(accountId).query((rs, n) -> ledgerQty.put(rs.getString(1), rs.getBigDecimal(2))).list();
		Map<String, BigDecimal> positionQty = new java.util.HashMap<>();
		jdbc.sql("SELECT symbol, qty FROM positions WHERE account_id = ?")
			.param(accountId)
			.query((rs, n) -> positionQty.put(rs.getString(1), rs.getBigDecimal(2)))
			.list();
		assertThat(positionQty.keySet()).isEqualTo(ledgerQty.keySet());
		positionQty.forEach((s, q) -> assertThat(q).isEqualByComparingTo(ledgerQty.get(s)));
	}
}
