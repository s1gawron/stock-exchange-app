package com.stocksim.trading;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.stocksim.account.AccountService;
import com.stocksim.common.ApiException;
import com.stocksim.instrument.Instrument;
import com.stocksim.instrument.InstrumentRepository;
import com.stocksim.marketdata.MarketDataProperties;
import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TradingService {

	private static final BigDecimal MAX_QTY = BigDecimal.valueOf(1_000_000);

	private final AccountService accounts;
	private final InstrumentRepository instruments;
	private final MarketDataProvider marketData;
	private final QuoteService quotes;
	private final OrderExecutor executor;
	private final OrderRepository orders;
	private final Clock clock;
	private final Duration maxQuoteAge;

	TradingService(AccountService accounts, InstrumentRepository instruments, MarketDataProvider marketData,
			QuoteService quotes, OrderExecutor executor, OrderRepository orders, Clock clock,
			MarketDataProperties properties) {
		this.accounts = accounts;
		this.instruments = instruments;
		this.marketData = marketData;
		this.quotes = quotes;
		this.executor = executor;
		this.orders = orders;
		this.clock = clock;
		this.maxQuoteAge = properties.maxQuoteAge();
	}

	public OrderView placeMarketOrder(long userId, String symbol, OrderSide side, BigDecimal qty,
			String idempotencyKey) {
		validateQty(qty);
		String s = symbol.trim().toUpperCase(Locale.ROOT);
		Optional<Instrument> instrument = instruments.find(s);
		if (instrument.isEmpty() || !instrument.get().active()) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Unknown or inactive symbol: " + s);
		}
		var req = new OrderExecutor.Request(accounts.forUser(userId).id(), s, side, qty, idempotencyKey);

		// Checks that need the vendor run outside the DB transaction, so no lock is held during HTTP calls.
		if (!marketData.marketStatus().open()) {
			return executor.reject(req, RejectReason.MARKET_CLOSED);
		}
		Optional<Quote> quote = quotes.quote(s);
		if (quote.isEmpty()) {
			return executor.reject(req, RejectReason.NO_QUOTE);
		}
		if (Duration.between(quote.get().timestamp(), clock.instant()).compareTo(maxQuoteAge) > 0) {
			return executor.reject(req, RejectReason.STALE_QUOTE);
		}
		return executor.execute(req, quote.get());
	}

	public List<OrderView> history(long userId, int page, int size) {
		int limit = Math.clamp(size, 1, 100);
		return orders.list(accounts.forUser(userId).id(), limit, Math.max(page, 0) * limit);
	}

	private static void validateQty(BigDecimal qty) {
		if (qty == null || qty.signum() <= 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
		}
		if (qty.stripTrailingZeros().scale() > 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Only whole shares are supported for now");
		}
		if (qty.compareTo(MAX_QTY) > 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity is too large");
		}
	}
}
