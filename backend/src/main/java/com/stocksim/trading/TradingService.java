package com.stocksim.trading;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.stocksim.account.AccountService;
import com.stocksim.common.ApiException;
import com.stocksim.instrument.InstrumentService;
import com.stocksim.instrument.InstrumentSummary;
import com.stocksim.marketdata.MarketDataProperties;
import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;
import com.stocksim.marketdata.UsMarketCalendar;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TradingService {

	/** What the user asked for. Prices are null when not used by the type. */
	public record PlaceOrder(String symbol, OrderSide side, OrderType type, BigDecimal qty, BigDecimal limitPrice,
			BigDecimal stopPrice, TimeInForce timeInForce) {

		public static PlaceOrder market(String symbol, OrderSide side, BigDecimal qty) {
			return new PlaceOrder(symbol, side, OrderType.MARKET, qty, null, null, null);
		}
	}

	private static final BigDecimal MAX_QTY = BigDecimal.valueOf(1_000_000);
	private static final BigDecimal MAX_PRICE = BigDecimal.valueOf(1_000_000);

	private final AccountService accounts;
	private final InstrumentService instruments;
	private final MarketDataProvider marketData;
	private final QuoteService quotes;
	private final OrderExecutor executor;
	private final OrderRepository orders;
	private final OrderMatcher matcher;
	private final UsMarketCalendar calendar;
	private final Clock clock;
	private final Duration maxQuoteAge;

	TradingService(AccountService accounts, InstrumentService instruments, MarketDataProvider marketData,
			QuoteService quotes, OrderExecutor executor, OrderRepository orders, OrderMatcher matcher,
			UsMarketCalendar calendar, Clock clock, MarketDataProperties properties) {
		this.accounts = accounts;
		this.instruments = instruments;
		this.marketData = marketData;
		this.quotes = quotes;
		this.executor = executor;
		this.orders = orders;
		this.matcher = matcher;
		this.calendar = calendar;
		this.clock = clock;
		this.maxQuoteAge = properties.maxQuoteAge();
	}

	/** Kept for callers that only place market orders. */
	public OrderView placeMarketOrder(long userId, String symbol, OrderSide side, BigDecimal qty,
			String idempotencyKey) {
		return placeOrder(userId, PlaceOrder.market(symbol, side, qty), idempotencyKey);
	}

	public OrderView placeOrder(long userId, PlaceOrder cmd, String idempotencyKey) {
		validate(cmd);
		String s = cmd.symbol().trim().toUpperCase(Locale.ROOT);
		Optional<InstrumentSummary> instrument = instruments.find(s);
		if (instrument.isEmpty() || !instrument.get().active()) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Unknown or inactive symbol: " + s);
		}
		long accountId = accounts.forUser(userId).id();

		if (cmd.type() != OrderType.MARKET) {
			TimeInForce tif = cmd.timeInForce() == null ? TimeInForce.DAY : cmd.timeInForce();
			Instant expiresAt = tif == TimeInForce.DAY ? calendar.nextSessionClose(clock.instant()) : null;
			var req = new OrderExecutor.PlaceRequest(accountId, s, cmd.side(), cmd.type(), cmd.qty(), cmd.limitPrice(),
					cmd.stopPrice(), tif, expiresAt, idempotencyKey);
			OrderView placed = executor.placePending(req);
			if (placed.status() == OrderStatus.OPEN) {
				// A limit already in the money (or a stop already passed) fills right away during market hours.
				matcher.matchSymbol(s);
				return orders.get(placed.id());
			}
			return placed;
		}

		var req = new OrderExecutor.PlaceRequest(accountId, s, cmd.side(), OrderType.MARKET, cmd.qty(), null, null,
				null, null, idempotencyKey);
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
		return executor.executeMarket(req, quote.get().price(), quote.get().timestamp());
	}

	public OrderView cancel(long userId, long orderId) {
		return executor.cancel(accounts.forUser(userId).id(), orderId);
	}

	public List<OrderView> history(long userId, OrderStatus status, int page, int size) {
		int limit = Math.clamp(size, 1, 100);
		return orders.list(accounts.forUser(userId).id(), status, limit, Math.max(page, 0) * limit);
	}

	private static void validate(PlaceOrder cmd) {
		BigDecimal qty = cmd.qty();
		if (qty == null || qty.signum() <= 0) {
			throw bad("Quantity must be positive");
		}
		if (qty.stripTrailingZeros().scale() > 0) {
			throw bad("Only whole shares are supported for now");
		}
		if (qty.compareTo(MAX_QTY) > 0) {
			throw bad("Quantity is too large");
		}
		switch (cmd.type()) {
			case MARKET -> {
				if (cmd.limitPrice() != null || cmd.stopPrice() != null || cmd.timeInForce() != null) {
					throw bad("Market orders take no limit price, stop price or time in force");
				}
			}
			case LIMIT -> {
				checkPrice(cmd.limitPrice(), "Limit price");
				if (cmd.stopPrice() != null) {
					throw bad("Limit orders take no stop price");
				}
			}
			case STOP -> {
				checkPrice(cmd.stopPrice(), "Stop price");
				if (cmd.limitPrice() != null) {
					throw bad("Stop orders take no limit price");
				}
			}
		}
	}

	private static void checkPrice(BigDecimal price, String name) {
		if (price == null || price.signum() <= 0) {
			throw bad(name + " must be positive");
		}
		if (price.stripTrailingZeros().scale() > 4) {
			throw bad(name + " can have at most 4 decimals");
		}
		if (price.compareTo(MAX_PRICE) > 0) {
			throw bad(name + " is too large");
		}
	}

	private static ApiException bad(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}
}
