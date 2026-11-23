package com.stocksim.instrument;

/** JSON shape of an instrument in the REST API. Component names and order must stay stable. */
public record InstrumentView(String symbol, String displaySymbol, String name, String type, String currency,
		String exchange, String logoUrl, String webUrl, String industry, boolean active) {
}
