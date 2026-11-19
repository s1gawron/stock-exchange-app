package com.stocksim.trading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.stocksim.IntegrationTest;
import com.stocksim.account.AccountService;
import com.stocksim.common.ApiException;
import com.stocksim.marketdata.PriceTick;
import com.stocksim.marketdata.QuoteService;
import com.stocksim.marketdata.TickStore;
import com.stocksim.marketdata.UsMarketCalendar;
import com.stocksim.portfolio.PortfolioService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

class PendingOrdersIntegrationTest extends IntegrationTest {

	@Autowired
	TradingService trading;

	@Autowired
	OrderMatcher matcher;

	@Autowired
	OrderExpiryJob expiryJob;

	@Autowired
	PortfolioService portfolio;

	@Autowired
	AccountService accounts;

	@Autowired
	UsMarketCalendar calendar;

	@Autowired
	TickStore ticks;

	@Autowired
	QuoteService quotes;

	@Autowired
	JdbcClient jdbc;

	long userId;

	@BeforeEach
	void newUser() {
		userId = newUserWithAccount();
		setPrice("AAPL", "200.0000");
	}

	@AfterEach
	void reconciles() {
		Reconciliation.assertReconciles(jdbc, accounts.forUser(userId).id());
	}

	private long newUserWithAccount() {
		long id = jdbc.sql("INSERT INTO users (email, display_name, password_hash) VALUES (?, 'T', 'x') RETURNING id")
			.param(UUID.randomUUID() + "@test.local")
			.query(Long.class)
			.single();
		accounts.openAccount(id);
		return id;
	}

	private OrderView place(OrderSide side, OrderType type, int qty, String price, TimeInForce tif) {
		BigDecimal p = price == null ? null : new BigDecimal(price);
		var cmd = new TradingService.PlaceOrder("AAPL", side, type, BigDecimal.valueOf(qty),
				type == OrderType.LIMIT ? p : null, type == OrderType.STOP ? p : null, tif);
		return trading.placeOrder(userId, cmd, UUID.randomUUID().toString());
	}

	private OrderView market(OrderSide side, int qty) {
		return trading.placeMarketOrder(userId, "AAPL", side, BigDecimal.valueOf(qty), UUID.randomUUID().toString());
	}

	private OrderView reload(OrderView o) {
		return trading.history(userId, null, 0, 100).stream().filter(x -> x.id() == o.id()).findFirst().orElseThrow();
	}

	@Test
	void buyLimitReservesCashWaitsAndFillsWhenPriceDrops() {
		OrderView o = place(OrderSide.BUY, OrderType.LIMIT, 10, "190", TimeInForce.GTC);
		assertThat(o.status()).isEqualTo(OrderStatus.OPEN);
		assertThat(o.reservedCash()).isEqualByComparingTo("1900.00");
		assertThat(o.expiresAt()).isNull();
		var p = portfolio.get(userId);
		assertThat(p.availableCash()).isEqualByComparingTo("98100.00");
		assertThat(p.cash()).isEqualByComparingTo("100000.00");

		matcher.matchSymbol("AAPL"); // still 200: no fill
		assertThat(reload(o).status()).isEqualTo(OrderStatus.OPEN);

		setPrice("AAPL", "189.5000");
		matcher.matchSymbol("AAPL");
		OrderView filled = reload(o);
		assertThat(filled.status()).isEqualTo(OrderStatus.FILLED);
		assertThat(filled.fill().price()).isEqualByComparingTo("189.50"); // better than the limit
		assertThat(filled.reservedCash()).isZero();
		p = portfolio.get(userId);
		assertThat(p.cash()).isEqualByComparingTo("98105.00");
		assertThat(p.reservedCash()).isZero();
	}

	@Test
	void marketableLimitFillsRightAway() {
		OrderView o = place(OrderSide.BUY, OrderType.LIMIT, 1, "210", TimeInForce.DAY);
		assertThat(o.status()).isEqualTo(OrderStatus.FILLED);
		assertThat(o.fill().price()).isEqualByComparingTo("200");
	}

	@Test
	void sellLimitReservesSharesAndCancelReleasesThem() {
		market(OrderSide.BUY, 10);
		OrderView o = place(OrderSide.SELL, OrderType.LIMIT, 6, "250", TimeInForce.GTC);
		assertThat(o.status()).isEqualTo(OrderStatus.OPEN);
		assertThat(portfolio.get(userId).holdings().getFirst().reservedQty()).isEqualByComparingTo("6");

		// Only 4 shares are free: selling 5 more is rejected, also as a second pending order.
		assertThat(market(OrderSide.SELL, 5).rejectReason()).isEqualTo(RejectReason.INSUFFICIENT_SHARES);
		assertThat(place(OrderSide.SELL, OrderType.LIMIT, 5, "260", TimeInForce.GTC).rejectReason())
			.isEqualTo(RejectReason.INSUFFICIENT_SHARES);

		OrderView cancelled = trading.cancel(userId, o.id());
		assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(portfolio.get(userId).holdings().getFirst().reservedQty()).isZero();
		assertThat(market(OrderSide.SELL, 5).status()).isEqualTo(OrderStatus.FILLED);
	}

	@Test
	void sellLimitFillsWhenPriceRises() {
		market(OrderSide.BUY, 10);
		OrderView o = place(OrderSide.SELL, OrderType.LIMIT, 10, "220", TimeInForce.GTC);
		setPrice("AAPL", "221.0000");
		matcher.matchSymbol("AAPL");
		OrderView filled = reload(o);
		assertThat(filled.status()).isEqualTo(OrderStatus.FILLED);
		assertThat(filled.fill().realizedPnl()).isEqualByComparingTo("210.00");
		assertThat(portfolio.get(userId).holdings()).isEmpty();
	}

	@Test
	void buyStopReservesWithBufferAndTriggersOnRise() {
		OrderView o = place(OrderSide.BUY, OrderType.STOP, 10, "210", TimeInForce.GTC);
		assertThat(o.status()).isEqualTo(OrderStatus.OPEN);
		assertThat(o.reservedCash()).isEqualByComparingTo("2205.00"); // 10 * 210 * 1.05

		setPrice("AAPL", "212.0000");
		matcher.matchSymbol("AAPL");
		OrderView filled = reload(o);
		assertThat(filled.status()).isEqualTo(OrderStatus.FILLED);
		assertThat(filled.fill().price()).isEqualByComparingTo("212");
	}

	@Test
	void sellStopTriggersOnDrop() {
		market(OrderSide.BUY, 5);
		OrderView o = place(OrderSide.SELL, OrderType.STOP, 5, "180", TimeInForce.GTC);
		setPrice("AAPL", "185.0000");
		matcher.matchSymbol("AAPL");
		assertThat(reload(o).status()).isEqualTo(OrderStatus.OPEN);
		setPrice("AAPL", "179.0000");
		matcher.matchSymbol("AAPL");
		assertThat(reload(o).status()).isEqualTo(OrderStatus.FILLED);
		assertThat(reload(o).fill().realizedPnl()).isEqualByComparingTo("-105.00");
	}

	@Test
	void buyStopRejectedAtTriggerWhenGapCostsMoreThanAllCash() {
		// Stop above the current 200. Reserve 460 * 205 * 1.05 = 99015; leaves 985 free.
		OrderView o = place(OrderSide.BUY, OrderType.STOP, 460, "205", TimeInForce.GTC);
		assertThat(o.status()).isEqualTo(OrderStatus.OPEN);
		setPrice("AAPL", "220.0000"); // gap: fill would cost 101200 > all cash
		matcher.matchSymbol("AAPL");
		OrderView r = reload(o);
		assertThat(r.status()).isEqualTo(OrderStatus.REJECTED);
		assertThat(r.rejectReason()).isEqualTo(RejectReason.INSUFFICIENT_FUNDS);
		assertThat(portfolio.get(userId).reservedCash()).isZero();
	}

	@Test
	void reservationLargerThanFreeCashIsRejected() {
		OrderView o = place(OrderSide.BUY, OrderType.LIMIT, 600, "200", TimeInForce.GTC);
		assertThat(o.rejectReason()).isEqualTo(RejectReason.INSUFFICIENT_FUNDS);
	}

	@Test
	void ordersCanBePlacedWhileClosedAndFillOnlyWhenOpen() {
		fakeMarket.setMarketOpen(false);
		OrderView o = place(OrderSide.BUY, OrderType.LIMIT, 1, "210", TimeInForce.DAY);
		assertThat(o.status()).isEqualTo(OrderStatus.OPEN); // marketable, but market is closed
		matcher.matchSymbol("AAPL");
		assertThat(reload(o).status()).isEqualTo(OrderStatus.OPEN);

		fakeMarket.setMarketOpen(true);
		matcher.matchSymbol("AAPL");
		assertThat(reload(o).status()).isEqualTo(OrderStatus.FILLED);
	}

	@Test
	void dayOrderExpiresAtSessionCloseAndReleasesCash() {
		OrderView o = place(OrderSide.BUY, OrderType.LIMIT, 10, "150", TimeInForce.DAY);
		assertThat(o.expiresAt()).isEqualTo(calendar.nextSessionClose(Instant.now()));

		assertThat(expiryJob.expireDueOrders()).isZero();
		jdbc.sql("UPDATE orders SET expires_at = now() - interval '1 second' WHERE id = ?").param(o.id()).update();
		assertThat(expiryJob.expireDueOrders()).isEqualTo(1);
		assertThat(reload(o).status()).isEqualTo(OrderStatus.EXPIRED);
		assertThat(portfolio.get(userId).reservedCash()).isZero();
	}

	@Test
	void cancelRules() {
		OrderView o = place(OrderSide.BUY, OrderType.LIMIT, 1, "150", TimeInForce.GTC);
		long otherUser = newUserWithAccount();
		assertThatThrownBy(() -> trading.cancel(otherUser, o.id())).isInstanceOf(ApiException.class)
			.hasMessageContaining("not found");
		trading.cancel(userId, o.id());
		assertThatThrownBy(() -> trading.cancel(userId, o.id())).isInstanceOf(ApiException.class)
			.hasMessageContaining("open");
	}

	@Test
	void validatesPricesPerType() {
		var limitWithoutPrice = new TradingService.PlaceOrder("AAPL", OrderSide.BUY, OrderType.LIMIT, BigDecimal.ONE,
				null, null, TimeInForce.DAY);
		var marketWithTif = new TradingService.PlaceOrder("AAPL", OrderSide.BUY, OrderType.MARKET, BigDecimal.ONE,
				null, null, TimeInForce.GTC);
		var tooManyDecimals = new TradingService.PlaceOrder("AAPL", OrderSide.BUY, OrderType.LIMIT, BigDecimal.ONE,
				new BigDecimal("1.23456"), null, TimeInForce.DAY);
		for (var cmd : List.of(limitWithoutPrice, marketWithTif, tooManyDecimals)) {
			assertThatThrownBy(() -> trading.placeOrder(userId, cmd, UUID.randomUUID().toString()))
				.isInstanceOf(ApiException.class);
		}
	}

	@Test
	void parallelPendingBuysNeverOverReserve() throws Exception {
		// Each reserves 10000; only 10 fit into 100000.
		List<Callable<OrderView>> tasks = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			tasks.add(() -> place(OrderSide.BUY, OrderType.LIMIT, 100, "100", TimeInForce.GTC));
		}
		List<OrderView> results = new ArrayList<>();
		try (ExecutorService pool = Executors.newFixedThreadPool(tasks.size())) {
			for (Future<OrderView> f : pool.invokeAll(tasks)) {
				results.add(f.get());
			}
		}
		assertThat(results).filteredOn(o -> o.status() == OrderStatus.OPEN).hasSize(10);
		assertThat(portfolio.get(userId).availableCash()).isEqualByComparingTo("0.00");
	}

	@Test
	void newerLiveTickOverridesCachedQuote() {
		quotes.quote("AAPL"); // caches the 200 snapshot
		ticks.record(new PriceTick("AAPL", new BigDecimal("201.25"), Instant.now().plusSeconds(1)));
		var q = quotes.quote("AAPL").orElseThrow();
		assertThat(q.price()).isEqualByComparingTo("201.25");
		assertThat(q.change()).isEqualByComparingTo("-28.75"); // vs fake previous close 230
	}
}
