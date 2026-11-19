package com.stocksim.performance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/**
 * Writes one snapshot per account for a trading day, plus the benchmark close. Runs after the close, when no orders
 * can fill, so account rows do not change while it reads them. Existing rows for the day are kept (idempotent).
 */
@Service
public class SnapshotService {

	private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);
	private static final int PRICE_ATTEMPTS = 5;

	private record PositionRow(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis) {
	}

	private record AccountRow(long id, BigDecimal cash, BigDecimal deposits) {
	}

	private final JdbcClient jdbc;
	private final QuoteService quotes;
	private final String benchmarkSymbol;
	private final long retryDelayMillis;

	SnapshotService(JdbcClient jdbc, QuoteService quotes, PerformanceProperties properties) {
		this.jdbc = jdbc;
		this.quotes = quotes;
		this.benchmarkSymbol = properties.benchmarkSymbol();
		this.retryDelayMillis = properties.priceRetryDelay().toMillis();
	}

	public boolean exists(LocalDate day) {
		return jdbc.sql("""
				SELECT EXISTS (SELECT 1 FROM benchmark_closes WHERE day = :day)
				    OR EXISTS (SELECT 1 FROM account_snapshots WHERE day = :day)
				""")
			.param("day", Date.valueOf(day))
			.query(Boolean.class)
			.single();
	}

	/** Returns the number of account snapshots written. */
	public int snapshot(LocalDate day) {
		List<PositionRow> positions = jdbc.sql("SELECT account_id, symbol, qty, cost_basis FROM positions")
			.query((rs, n) -> new PositionRow(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3), rs.getBigDecimal(4)))
			.list();
		Set<String> symbols = new LinkedHashSet<>();
		symbols.add(benchmarkSymbol);
		positions.forEach(p -> symbols.add(p.symbol()));
		Map<String, BigDecimal> prices = new HashMap<>();
		for (String s : symbols) {
			price(s).ifPresent(p -> prices.put(s, p));
		}

		Map<Long, BigDecimal> marketValue = new HashMap<>();
		Map<Long, Boolean> complete = new HashMap<>();
		for (PositionRow p : positions) {
			BigDecimal price = prices.get(p.symbol());
			// No price: value at cost, and flag the snapshot.
			BigDecimal value = price == null ? p.costBasis() : price.multiply(p.qty()).setScale(2, RoundingMode.HALF_UP);
			marketValue.merge(p.accountId(), value, BigDecimal::add);
			if (price == null) {
				complete.put(p.accountId(), false);
			}
		}

		List<AccountRow> accounts = jdbc.sql("""
				SELECT a.id, a.cash_balance,
				       (SELECT COALESCE(SUM(cash_amount), 0) FROM ledger_entries l
				        WHERE l.account_id = a.id AND l.type = 'DEPOSIT') AS deposits
				FROM accounts a
				""").query((rs, n) -> new AccountRow(rs.getLong(1), rs.getBigDecimal(2), rs.getBigDecimal(3))).list();
		int written = 0;
		for (AccountRow a : accounts) {
			BigDecimal value = marketValue.getOrDefault(a.id(), BigDecimal.ZERO);
			written += jdbc.sql("""
					INSERT INTO account_snapshots (account_id, day, cash, market_value, equity, deposits, prices_complete)
					VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (account_id, day) DO NOTHING
					""")
				.params(a.id(), Date.valueOf(day), a.cash(), value, a.cash().add(value), a.deposits(),
						complete.getOrDefault(a.id(), true))
				.update();
		}

		BigDecimal benchmark = prices.get(benchmarkSymbol);
		if (benchmark != null) {
			jdbc.sql("INSERT INTO benchmark_closes (symbol, day, close) VALUES (?, ?, ?) ON CONFLICT DO NOTHING")
				.params(benchmarkSymbol, Date.valueOf(day), benchmark)
				.update();
		}
		else {
			log.warn("No {} price for {}; benchmark close missing", benchmarkSymbol, day);
		}
		log.info("Snapshots for {}: {} accounts, {} of {} prices", day, written, prices.size(), symbols.size());
		return written;
	}

	/** Retries because the vendor rate limit may refuse calls while many symbols are priced. */
	private Optional<BigDecimal> price(String symbol) {
		for (int attempt = 1; attempt <= PRICE_ATTEMPTS; attempt++) {
			try {
				return quotes.quote(symbol).map(Quote::price);
			}
			catch (MarketDataUnavailableException ex) {
				if (attempt == PRICE_ATTEMPTS) {
					log.warn("No price for {} after {} attempts: {}", symbol, attempt, ex.getMessage());
					return Optional.empty();
				}
				sleep();
			}
		}
		return Optional.empty();
	}

	private void sleep() {
		try {
			Thread.sleep(retryDelayMillis);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
