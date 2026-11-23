package com.stocksim.instrument;

/** What other modules need to know about an instrument. */
public record InstrumentSummary(String symbol, String name, boolean active) {
}
