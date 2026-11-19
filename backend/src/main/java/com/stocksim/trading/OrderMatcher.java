package com.stocksim.trading;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.stocksim.account.AccountRepository;
import com.stocksim.marketdata.MarketDataProperties;
import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.PriceTick;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Fills OPEN limit/stop orders when the price reaches them, only during regular market hours.
 * Triggers: live ticks (batched once per second) and a periodic sweep for symbols without live data.
 *
 * <pre>
 * BUY  LIMIT: price <= limit    SELL LIMIT: price >= limit
 * BUY  STOP : price >= stop     SELL STOP : price <= stop
 * </pre>
 * Fills happen at the observed price (no slippage). A gap past the limit fills at the better observed price.
 */
@Component
public class OrderMatcher {

	private static final Logger log = LoggerFactory.getLogger(OrderMatcher.class);

	private final OrderRepository orders;
	private final OrderExecutor executor;
	private final AccountRepository accounts;
	private final MarketDataProvider marketData;
	private final QuoteService quotes;
	private final ApplicationEventPublisher events;
	private final Clock clock;
	private final Duration maxQuoteAge;
	private final Set<String> tickedSymbols = ConcurrentHashMap.newKeySet();

	OrderMatcher(OrderRepository orders, OrderExecutor executor, AccountRepository accounts,
			MarketDataProvider marketData, QuoteService quotes, ApplicationEventPublisher events, Clock clock,
			MarketDataProperties properties) {
		this.orders = orders;
		this.executor = executor;
		this.accounts = accounts;
		this.marketData = marketData;
		this.quotes = quotes;
		this.events = events;
		this.clock = clock;
		this.maxQuoteAge = properties.maxQuoteAge();
	}

	/** Called on the feed thread for every trade: only remember the symbol, match later in batch. */
	@EventListener
	void onTick(PriceTick tick) {
		tickedSymbols.add(tick.symbol());
	}

	@Scheduled(fixedDelayString = "${trading.matcher.tick-interval:PT1S}",
			initialDelayString = "${trading.matcher.tick-interval:PT1S}")
	void processTicks() {
		if (tickedSymbols.isEmpty()) {
			return;
		}
		List<String> symbols = new ArrayList<>(tickedSymbols);
		tickedSymbols.removeAll(symbols);
		symbols.forEach(this::matchSymbol);
	}

	@Scheduled(fixedDelayString = "${trading.matcher.sweep-interval:PT15S}",
			initialDelayString = "${trading.matcher.sweep-interval:PT15S}")
	void sweep() {
		if (!marketOpen()) {
			return;
		}
		orders.openSymbols().forEach(this::matchSymbol);
	}

	/** Checks all OPEN orders of one symbol against the current price and fills those that trigger. */
	public void matchSymbol(String symbol) {
		if (!marketOpen()) {
			return;
		}
		List<OrderRepository.OrderRow> open = orders.openForSymbol(symbol);
		if (open.isEmpty()) {
			return;
		}
		Optional<Quote> quote;
		try {
			quote = quotes.quote(symbol);
		}
		catch (MarketDataUnavailableException ex) {
			log.debug("No price for {} while matching: {}", symbol, ex.getMessage());
			return;
		}
		if (quote.isEmpty()
				|| Duration.between(quote.get().timestamp(), clock.instant()).compareTo(maxQuoteAge) > 0) {
			return;
		}
		BigDecimal price = quote.get().price();
		for (OrderRepository.OrderRow o : open) {
			if (triggered(o, price)) {
				executor.fillOpen(o.id(), price, quote.get().timestamp())
					.ifPresent(v -> events.publishEvent(new OrderUpdatedEvent(accounts.userIdOf(o.accountId()), v)));
			}
		}
	}

	static boolean triggered(OrderRepository.OrderRow o, BigDecimal price) {
		return switch (o.type()) {
			case LIMIT -> o.side() == OrderSide.BUY ? price.compareTo(o.limitPrice()) <= 0
					: price.compareTo(o.limitPrice()) >= 0;
			case STOP -> o.side() == OrderSide.BUY ? price.compareTo(o.stopPrice()) >= 0
					: price.compareTo(o.stopPrice()) <= 0;
			case MARKET -> false;
		};
	}

	private boolean marketOpen() {
		try {
			return marketData.marketStatus().open();
		}
		catch (RuntimeException ex) {
			return false;
		}
	}
}
