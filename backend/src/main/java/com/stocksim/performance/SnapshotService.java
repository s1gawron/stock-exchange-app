package com.stocksim.performance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.stocksim.account.AccountBalance;
import com.stocksim.account.AccountService;
import com.stocksim.ledger.LedgerService;
import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;
import com.stocksim.portfolio.PositionHolding;
import com.stocksim.portfolio.PositionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Writes one snapshot per account for a trading day, plus the benchmark close. Runs after the close, when no orders
 * can fill, so account rows do not change while it reads them. Existing rows for the day are kept (idempotent).
 */
@Service
public class SnapshotService {

	private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);
	private static final int PRICE_ATTEMPTS = 5;

	private final SnapshotRepository repository;
	private final PositionService positions;
	private final AccountService accounts;
	private final LedgerService ledger;
	private final QuoteService quotes;
	private final String benchmarkSymbol;
	private final long retryDelayMillis;

	SnapshotService(SnapshotRepository repository, PositionService positions, AccountService accounts,
			LedgerService ledger, QuoteService quotes, PerformanceProperties properties) {
		this.repository = repository;
		this.positions = positions;
		this.accounts = accounts;
		this.ledger = ledger;
		this.quotes = quotes;
		this.benchmarkSymbol = properties.benchmarkSymbol();
		this.retryDelayMillis = properties.priceRetryDelay().toMillis();
	}

	public boolean exists(LocalDate day) {
		return repository.exists(day);
	}

	/** Returns the number of account snapshots written. */
	public int snapshot(LocalDate day) {
		List<PositionHolding> held = positions.all();
		Set<String> symbols = new LinkedHashSet<>();
		symbols.add(benchmarkSymbol);
		held.forEach(p -> symbols.add(p.symbol()));
		Map<String, BigDecimal> prices = new HashMap<>();
		for (String s : symbols) {
			price(s).ifPresent(p -> prices.put(s, p));
		}

		Map<Long, BigDecimal> marketValue = new HashMap<>();
		Map<Long, Boolean> complete = new HashMap<>();
		for (PositionHolding p : held) {
			BigDecimal price = prices.get(p.symbol());
			// No price: value at cost, and flag the snapshot.
			BigDecimal value = price == null ? p.costBasis() : price.multiply(p.qty()).setScale(2, RoundingMode.HALF_UP);
			marketValue.merge(p.accountId(), value, BigDecimal::add);
			if (price == null) {
				complete.put(p.accountId(), false);
			}
		}

		List<AccountBalance> balances = accounts.allBalances();
		Map<Long, BigDecimal> deposits = ledger.depositsByAccount();
		int written = 0;
		for (AccountBalance a : balances) {
			BigDecimal value = marketValue.getOrDefault(a.id(), BigDecimal.ZERO);
			written += repository.insertSnapshot(a.id(), day, a.cashBalance(), value, a.cashBalance().add(value),
					deposits.getOrDefault(a.id(), BigDecimal.ZERO), complete.getOrDefault(a.id(), true));
		}

		BigDecimal benchmark = prices.get(benchmarkSymbol);
		if (benchmark != null) {
			repository.insertBenchmarkClose(benchmarkSymbol, day, benchmark);
		}
		else {
			log.warn("No {} price for {}; benchmark close missing", benchmarkSymbol, day);
		}
		log.info("Snapshots for {}: {} accounts, {} of {} prices", day, written, prices.size(), symbols.size());
		return written;
	}

	/** Latest snapshot day inside [from, to], if any. */
	public Optional<LocalDate> lastSnapshotDayBetween(LocalDate from, LocalDate to) {
		return repository.latestDayBetween(from, to);
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
