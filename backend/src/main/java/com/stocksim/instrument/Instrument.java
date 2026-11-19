package com.stocksim.instrument;

public record Instrument(String symbol, String displaySymbol, String name, String type, String currency,
		String exchange, String logoUrl, String webUrl, String industry, boolean active) {
}
