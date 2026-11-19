package com.stocksim.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.stocksim.IntegrationTest;
import com.stocksim.TestUsers;
import com.stocksim.account.AccountService;
import com.stocksim.trading.OrderSide;
import com.stocksim.trading.TradingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Uses far-future days (one block per test) so tests do not see each other's snapshots. */
class PerformanceIntegrationTest extends IntegrationTest {

	private static final AtomicInteger YEAR = new AtomicInteger(2100);

	@Autowired
	SnapshotService snapshots;

	@Autowired
	AnalyticsService analytics;

	@Autowired
	LeaderboardService leaderboard;

	@Autowired
	TradingService trading;

	@Autowired
	AccountService accounts;

	@Autowired
	JdbcClient jdbc;

	private void buy(long userId, String symbol, int qty) {
		trading.placeMarketOrder(userId, symbol, OrderSide.BUY, BigDecimal.valueOf(qty), UUID.randomUUID().toString());
	}

	private BigDecimal snapshotEquity(long userId, LocalDate day) {
		return jdbc.sql("SELECT s.equity FROM account_snapshots s JOIN accounts a ON a.id = s.account_id "
				+ "WHERE a.user_id = ? AND s.day = ?").params(userId, Date.valueOf(day)).query(BigDecimal.class).single();
	}

	@Test
	void snapshotValuesAccountsAndIsIdempotent() {
		LocalDate day = LocalDate.of(YEAR.getAndIncrement(), 1, 5);
		long user = TestUsers.create(jdbc, accounts, "Snap");
		setPrice("AAPL", "200");
		setPrice("SPY", "500");
		buy(user, "AAPL", 10);
		setPrice("AAPL", "210");

		snapshots.snapshot(day);
		assertThat(snapshotEquity(user, day)).isEqualByComparingTo("100100.00"); // 98000 cash + 2100
		assertThat(snapshots.exists(day)).isTrue();

		setPrice("AAPL", "300");
		snapshots.snapshot(day); // second run keeps the first values
		assertThat(snapshotEquity(user, day)).isEqualByComparingTo("100100.00");
		assertThat(jdbc.sql("SELECT close FROM benchmark_closes WHERE symbol = 'SPY' AND day = ?")
			.param(Date.valueOf(day)).query(BigDecimal.class).single()).isEqualByComparingTo("500");
	}

	@Test
	void analyticsReturnsDrawdownAndBenchmark() {
		int y = YEAR.getAndIncrement();
		long user = TestUsers.create(jdbc, accounts, "Ana");
		setPrice("AAPL", "100");
		buy(user, "AAPL", 500); // 50000 in AAPL, 50000 cash

		String[][] days = { // day, AAPL, SPY
				{ "01-05", "110", "500" }, // equity 105000
				{ "01-06", "130", "505" }, // 115000 (peak)
				{ "01-07", "90", "510" },  // 95000 (trough: -17.39% from 115000)
				{ "01-08", "120", "520" }, // 110000
		};
		for (String[] d : days) {
			setPrice("AAPL", d[1]);
			setPrice("SPY", d[2]);
			snapshots.snapshot(LocalDate.parse(y + "-" + d[0]));
		}

		var a = analytics.forUser(user);
		assertThat(a.tradingDays()).isEqualTo(4);
		assertThat(a.returnPercent()).isEqualByComparingTo("10.00");
		assertThat(a.periodReturnPercent()).isEqualByComparingTo("4.76"); // 110000 / 105000 - 1
		assertThat(a.benchmarkReturnPercent()).isEqualByComparingTo("4.00"); // 520 / 500 - 1
		assertThat(a.vsBenchmarkPercent()).isEqualByComparingTo("0.76");
		assertThat(a.maxDrawdownPercent()).isEqualByComparingTo("-17.39");
		assertThat(a.maxDrawdownPeakDay()).isEqualTo(LocalDate.parse(y + "-01-06"));
		assertThat(a.maxDrawdownTroughDay()).isEqualTo(LocalDate.parse(y + "-01-07"));
		assertThat(a.bestDay().day()).isEqualTo(LocalDate.parse(y + "-01-08")); // +15.79%
		assertThat(a.worstDay().dayChangePercent()).isEqualByComparingTo("-17.39");
		assertThat(a.days().getFirst().day()).isEqualTo(LocalDate.parse(y + "-01-08")); // newest first
	}

	@Test
	void analyticsWithoutSnapshotsIsEmpty() {
		long user = TestUsers.create(jdbc, accounts, "New");
		var a = analytics.forUser(user);
		assertThat(a.days()).isEmpty();
		assertThat(a.returnPercent()).isNull();
	}

	@Test
	void leaderboardRanksByReturnAndMarksMe() {
		// The leaderboard shows the latest snapshot day overall: use a day after every other test's days.
		LocalDate day = LocalDate.of(2999, 1, 5);
		long winner = TestUsers.create(jdbc, accounts, "Winner");
		long loser = TestUsers.create(jdbc, accounts, "Loser");
		setPrice("AAPL", "100");
		buy(winner, "AAPL", 900); // 90000 in AAPL, 10000 cash
		buy(loser, "AAPL", 900);
		setPrice("MSFT", "100");
		buy(loser, "MSFT", 100);  // loser puts the other 10000 into MSFT
		setPrice("AAPL", "150");  // winner: 135000 + 10000 = 145000 (+45%)
		setPrice("MSFT", "1");    // loser:  135000 + 100 = 135100 (+35.1%)
		snapshots.snapshot(day);

		var lb = leaderboard.get(loser, 100);
		assertThat(lb.asOf()).isEqualTo(day);
		var w = lb.top().stream().filter(e -> e.displayName().equals("Winner")).findFirst().orElseThrow();
		assertThat(w.returnPercent()).isEqualByComparingTo("45.00");
		assertThat(lb.me().returnPercent()).isEqualByComparingTo("35.10");
		assertThat(lb.me().displayName()).isEqualTo("Loser");
		assertThat(lb.me().me()).isTrue();
		assertThat(lb.me().rank()).isGreaterThan(w.rank());
		assertThat(w.me()).isFalse();
	}
}
