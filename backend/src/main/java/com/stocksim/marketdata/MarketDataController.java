package com.stocksim.marketdata;

import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class MarketDataController {

	private final MarketDataProvider provider;
	private final QuoteService quotes;

	MarketDataController(MarketDataProvider provider, QuoteService quotes) {
		this.provider = provider;
		this.quotes = quotes;
	}

	@GetMapping("/market/status")
	MarketStatus status() {
		return provider.marketStatus();
	}

	@GetMapping("/quotes/{symbol}")
	ResponseEntity<Quote> quote(@PathVariable String symbol) {
		return ResponseEntity.of(quotes.quote(symbol.toUpperCase(Locale.ROOT)));
	}
}
