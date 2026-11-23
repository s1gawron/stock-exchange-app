package com.stocksim.watchlist;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.stocksim.common.ApiException;
import com.stocksim.instrument.InstrumentService;
import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class WatchlistService {

	private static final int MAX_ITEMS = 50;

	/** Price fields are null when no quote is available. */
	public record Item(String symbol, String name, BigDecimal price, BigDecimal change, BigDecimal changePercent) {
	}

	private final WatchlistRepository repository;
	private final InstrumentService instruments;
	private final QuoteService quotes;

	WatchlistService(WatchlistRepository repository, InstrumentService instruments, QuoteService quotes) {
		this.repository = repository;
		this.instruments = instruments;
		this.quotes = quotes;
	}

	public List<Item> list(long userId) {
		List<String> symbols = repository.symbolsOf(userId);
		Map<String, String> names = instruments.names(symbols);
		List<Item> items = new ArrayList<>();
		for (String symbol : symbols) {
			if (!names.containsKey(symbol)) {
				continue;
			}
			Optional<Quote> q = quote(symbol);
			items.add(new Item(symbol, names.get(symbol), q.map(Quote::price).orElse(null),
					q.map(Quote::change).orElse(null), q.map(Quote::changePercent).orElse(null)));
		}
		return items;
	}

	public void add(long userId, String symbol) {
		String s = symbol.toUpperCase(Locale.ROOT);
		if (instruments.find(s).isEmpty()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Unknown symbol: " + s);
		}
		if (repository.count(userId) >= MAX_ITEMS) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Watchlist is full (max " + MAX_ITEMS + ")");
		}
		repository.add(userId, s);
	}

	public void remove(long userId, String symbol) {
		repository.remove(userId, symbol.toUpperCase(Locale.ROOT));
	}

	/** Symbols on any watchlist, most watched first (used to choose the live feed subscriptions). */
	public List<String> symbolsByPopularity() {
		return repository.symbolsByPopularity();
	}

	private Optional<Quote> quote(String symbol) {
		try {
			return quotes.quote(symbol);
		}
		catch (MarketDataUnavailableException ex) {
			return Optional.empty();
		}
	}
}
