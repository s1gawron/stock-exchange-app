package com.stocksim.performance;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Owns the {@code account_snapshots} and {@code benchmark_closes} tables. */
@Repository
class SnapshotRepository {

	/** One day of an account: equity and deposits as of that day's close. */
	record SnapshotRow(LocalDate day, BigDecimal equity, BigDecimal deposits, boolean complete) {
	}

	private final JdbcClient jdbc;

	SnapshotRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	boolean exists(LocalDate day) {
		return jdbc.sql("""
				SELECT EXISTS (SELECT 1 FROM benchmark_closes WHERE day = :day)
				    OR EXISTS (SELECT 1 FROM account_snapshots WHERE day = :day)
				""")
			.param("day", Date.valueOf(day))
			.query(Boolean.class)
			.single();
	}

	/** Keeps an existing row for the same account and day. Returns the number of rows written (0 or 1). */
	int insertSnapshot(long accountId, LocalDate day, BigDecimal cash, BigDecimal marketValue, BigDecimal equity,
			BigDecimal deposits, boolean pricesComplete) {
		return jdbc.sql("""
				INSERT INTO account_snapshots (account_id, day, cash, market_value, equity, deposits, prices_complete)
				VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (account_id, day) DO NOTHING
				""").params(accountId, Date.valueOf(day), cash, marketValue, equity, deposits, pricesComplete).update();
	}

	void insertBenchmarkClose(String symbol, LocalDate day, BigDecimal close) {
		jdbc.sql("INSERT INTO benchmark_closes (symbol, day, close) VALUES (?, ?, ?) ON CONFLICT DO NOTHING")
			.params(symbol, Date.valueOf(day), close)
			.update();
	}

	/** The latest {@code limit} snapshots of the account, oldest first. */
	List<SnapshotRow> latestOf(long accountId, int limit) {
		return jdbc.sql("""
				SELECT day, equity, deposits, prices_complete FROM (
				  SELECT * FROM account_snapshots WHERE account_id = ? ORDER BY day DESC LIMIT ?) s
				ORDER BY day
				""").params(accountId, limit)
			.query((rs, n) -> new SnapshotRow(rs.getDate(1).toLocalDate(), rs.getBigDecimal(2),
					rs.getBigDecimal(3), rs.getBoolean(4)))
			.list();
	}

	Map<LocalDate, BigDecimal> benchmarkCloses(String symbol, LocalDate from, LocalDate to) {
		Map<LocalDate, BigDecimal> closes = new HashMap<>();
		jdbc.sql("SELECT day, close FROM benchmark_closes WHERE symbol = ? AND day IN (?, ?)")
			.params(symbol, Date.valueOf(from), Date.valueOf(to))
			.query((rs, n) -> closes.put(rs.getDate(1).toLocalDate(), rs.getBigDecimal(2)))
			.list();
		return closes;
	}

	Optional<LocalDate> latestDay() {
		return jdbc.sql("SELECT max(day) FROM account_snapshots")
			.query(Date.class)
			.optional()
			.map(Date::toLocalDate);
	}

	Optional<LocalDate> latestDayBetween(LocalDate from, LocalDate to) {
		return jdbc.sql("SELECT max(day) FROM account_snapshots WHERE day BETWEEN ? AND ?")
			.params(Date.valueOf(from), Date.valueOf(to))
			.query(Date.class)
			.optional()
			.map(Date::toLocalDate);
	}

	int countOn(LocalDate day) {
		return jdbc.sql("SELECT count(*) FROM account_snapshots WHERE day = ?")
			.param(Date.valueOf(day))
			.query(Integer.class)
			.single();
	}
}
