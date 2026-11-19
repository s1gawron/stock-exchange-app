package com.stocksim.performance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stocksim.account.AccountService;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/** Performance numbers from the daily snapshots (as of the last close). */
@Service
public class AnalyticsService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
	private static final int MAX_DAYS = 400;

	/** @param returnPercent cumulative return since the account opened (equity / deposits - 1) */
	public record Day(LocalDate day, BigDecimal equity, BigDecimal dayChangePercent, BigDecimal returnPercent,
			boolean pricesComplete) {
	}

	/**
	 * Numbers are null when there is not enough data yet (e.g. fewer than 2 snapshots for period figures).
	 *
	 * @param periodReturnPercent    equity change from the first to the last snapshot
	 * @param benchmarkReturnPercent benchmark change over the same days
	 * @param maxDrawdownPercent     largest fall from a previous high (account opening counts as a high), negative
	 */
	public record Analytics(String benchmarkSymbol, LocalDate firstDay, LocalDate lastDay, Integer tradingDays,
			BigDecimal returnPercent, BigDecimal periodReturnPercent, BigDecimal benchmarkReturnPercent,
			BigDecimal vsBenchmarkPercent, BigDecimal maxDrawdownPercent, LocalDate maxDrawdownPeakDay,
			LocalDate maxDrawdownTroughDay, Day bestDay, Day worstDay, List<Day> days) {
	}

	private record Row(LocalDate day, BigDecimal equity, BigDecimal deposits, boolean complete) {
	}

	private final JdbcClient jdbc;
	private final AccountService accounts;
	private final String benchmark;

	AnalyticsService(JdbcClient jdbc, AccountService accounts, PerformanceProperties properties) {
		this.jdbc = jdbc;
		this.accounts = accounts;
		this.benchmark = properties.benchmarkSymbol();
	}

	public Analytics forUser(long userId) {
		long accountId = accounts.forUser(userId).id();
		List<Row> rows = jdbc.sql("""
				SELECT day, equity, deposits, prices_complete FROM (
				  SELECT * FROM account_snapshots WHERE account_id = ? ORDER BY day DESC LIMIT ?) s
				ORDER BY day
				""").params(accountId, MAX_DAYS)
			.query((rs, n) -> new Row(rs.getDate(1).toLocalDate(), rs.getBigDecimal(2), rs.getBigDecimal(3),
					rs.getBoolean(4)))
			.list();
		if (rows.isEmpty()) {
			return new Analytics(benchmark, null, null, 0, null, null, null, null, null, null, null, null, null,
					List.of());
		}

		List<Day> days = new ArrayList<>();
		BigDecimal peak = rows.getFirst().deposits(); // the account opened with this much
		LocalDate peakDay = null;
		BigDecimal maxDd = BigDecimal.ZERO;
		LocalDate ddPeakDay = null;
		LocalDate ddTroughDay = null;
		Row prev = null;
		for (Row r : rows) {
			BigDecimal dayChange = prev == null ? null : pct(r.equity(), prev.equity());
			days.add(new Day(r.day(), r.equity(), dayChange, pct(r.equity(), r.deposits()), r.complete()));
			if (r.equity().compareTo(peak) > 0) {
				peak = r.equity();
				peakDay = r.day();
			}
			BigDecimal dd = pct(r.equity(), peak);
			if (dd.compareTo(maxDd) < 0) {
				maxDd = dd;
				ddPeakDay = peakDay;
				ddTroughDay = r.day();
			}
			prev = r;
		}

		Row first = rows.getFirst();
		Row last = rows.getLast();
		BigDecimal periodReturn = rows.size() < 2 ? null : pct(last.equity(), first.equity());
		BigDecimal benchmarkReturn = rows.size() < 2 ? null : benchmarkReturn(first.day(), last.day());
		BigDecimal vs = periodReturn == null || benchmarkReturn == null ? null : periodReturn.subtract(benchmarkReturn);
		List<Day> withChange = days.stream().filter(d -> d.dayChangePercent() != null).toList();
		Day best = withChange.stream().max(Comparator.comparing(Day::dayChangePercent)).orElse(null);
		Day worst = withChange.stream().min(Comparator.comparing(Day::dayChangePercent)).orElse(null);

		return new Analytics(benchmark, first.day(), last.day(), rows.size(), pct(last.equity(), last.deposits()),
				periodReturn, benchmarkReturn, vs, maxDd, ddPeakDay, ddTroughDay, best, worst,
				days.reversed());
	}

	private BigDecimal benchmarkReturn(LocalDate from, LocalDate to) {
		Map<LocalDate, BigDecimal> closes = new HashMap<>();
		jdbc.sql("SELECT day, close FROM benchmark_closes WHERE symbol = ? AND day IN (?, ?)")
			.params(benchmark, Date.valueOf(from), Date.valueOf(to))
			.query((rs, n) -> closes.put(rs.getDate(1).toLocalDate(), rs.getBigDecimal(2)))
			.list();
		BigDecimal a = closes.get(from);
		BigDecimal b = closes.get(to);
		return a == null || b == null ? null : pct(b, a);
	}

	/** (value / base - 1) in percent, 2 decimals. */
	static BigDecimal pct(BigDecimal value, BigDecimal base) {
		if (base.signum() == 0) {
			return BigDecimal.ZERO;
		}
		return value.subtract(base).multiply(HUNDRED).divide(base, 2, RoundingMode.HALF_UP);
	}
}
