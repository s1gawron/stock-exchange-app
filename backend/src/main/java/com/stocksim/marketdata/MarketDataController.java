package com.stocksim.marketdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class MarketDataController {

	/** A quote plus the session its last trade happened in (e.g. PRE_MARKET while the market is closed). */
	record QuoteResponse(String symbol, BigDecimal price, BigDecimal change, BigDecimal changePercent,
			BigDecimal high, BigDecimal low, BigDecimal open, BigDecimal previousClose, Instant timestamp,
			TradingSession session) {
	}

	private final MarketDataProvider provider;
	private final QuoteService quotes;
	private final UsMarketCalendar calendar;

	MarketDataController(MarketDataProvider provider, QuoteService quotes, UsMarketCalendar calendar) {
		this.provider = provider;
		this.quotes = quotes;
		this.calendar = calendar;
	}

	@GetMapping("/market/status")
	MarketStatus status() {
		return provider.marketStatus();
	}

	@GetMapping("/quotes/{symbol}")
	ResponseEntity<QuoteResponse> quote(@PathVariable String symbol) {
		return ResponseEntity.of(quotes.quote(symbol.toUpperCase(Locale.ROOT))
			.map(q -> new QuoteResponse(q.symbol(), q.price(), q.change(), q.changePercent(), q.high(), q.low(),
					q.open(), q.previousClose(), q.timestamp(), calendar.sessionAt(q.timestamp()))));
	}
}
