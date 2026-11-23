package com.stocksim.instrument;

/** Row of the {@code instruments} table. Stays inside this module; callers get {@link InstrumentView} or {@link InstrumentSummary}. */
record InstrumentRow(String symbol, String displaySymbol, String name, String type, String currency,
		String exchange, String logoUrl, String webUrl, String industry, boolean active) {
}
