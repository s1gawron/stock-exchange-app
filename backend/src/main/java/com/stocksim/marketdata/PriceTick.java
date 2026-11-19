package com.stocksim.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

/** One trade from the live feed. Published as a Spring event. */
public record PriceTick(String symbol, BigDecimal price, Instant timestamp) {
}
