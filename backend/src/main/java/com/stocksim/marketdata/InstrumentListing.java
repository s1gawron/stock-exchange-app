package com.stocksim.marketdata;

/** One tradable symbol from the provider's symbol list. */
public record InstrumentListing(String symbol, String displaySymbol, String name, String type, String currency,
		String mic, String figi) {
}
