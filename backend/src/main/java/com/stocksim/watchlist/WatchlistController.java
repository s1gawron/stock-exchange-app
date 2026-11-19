package com.stocksim.watchlist;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.stocksim.auth.AppUser;
import com.stocksim.common.ApiException;
import com.stocksim.instrument.InstrumentRepository;
import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.Quote;
import com.stocksim.marketdata.QuoteService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlist")
class WatchlistController {

	private static final int MAX_ITEMS = 50;

	record Item(String symbol, String name, BigDecimal price, BigDecimal change, BigDecimal changePercent) {
	}

	private final JdbcClient jdbc;
	private final InstrumentRepository instruments;
	private final QuoteService quotes;

	WatchlistController(JdbcClient jdbc, InstrumentRepository instruments, QuoteService quotes) {
		this.jdbc = jdbc;
		this.instruments = instruments;
		this.quotes = quotes;
	}

	@GetMapping
	List<Item> list(@AuthenticationPrincipal AppUser user) {
		return jdbc.sql("""
				SELECT w.symbol, i.name FROM watchlist_items w JOIN instruments i ON i.symbol = w.symbol
				WHERE w.user_id = ? ORDER BY w.created_at
				""").param(user.id()).query((rs, n) -> {
			String symbol = rs.getString(1);
			Optional<Quote> q = quote(symbol);
			return new Item(symbol, rs.getString(2), q.map(Quote::price).orElse(null),
					q.map(Quote::change).orElse(null), q.map(Quote::changePercent).orElse(null));
		}).list();
	}

	@PutMapping("/{symbol}")
	ResponseEntity<Void> add(@AuthenticationPrincipal AppUser user, @PathVariable String symbol) {
		String s = symbol.toUpperCase(Locale.ROOT);
		if (instruments.find(s).isEmpty()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Unknown symbol: " + s);
		}
		int count = jdbc.sql("SELECT count(*) FROM watchlist_items WHERE user_id = ?")
			.param(user.id())
			.query(Integer.class)
			.single();
		if (count >= MAX_ITEMS) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "Watchlist is full (max " + MAX_ITEMS + ")");
		}
		jdbc.sql("INSERT INTO watchlist_items (user_id, symbol) VALUES (?, ?) ON CONFLICT DO NOTHING")
			.params(user.id(), s)
			.update();
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{symbol}")
	ResponseEntity<Void> remove(@AuthenticationPrincipal AppUser user, @PathVariable String symbol) {
		jdbc.sql("DELETE FROM watchlist_items WHERE user_id = ? AND symbol = ?")
			.params(user.id(), symbol.toUpperCase(Locale.ROOT))
			.update();
		return ResponseEntity.noContent().build();
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
