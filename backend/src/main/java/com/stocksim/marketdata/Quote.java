package com.stocksim.marketdata;

import java.math.BigDecimal;
import java.time.Instant;

/** Latest price snapshot for one symbol. {@code timestamp} is the time of the last trade. */
public record Quote(String symbol, BigDecimal price, BigDecimal change, BigDecimal changePercent, BigDecimal high,
		BigDecimal low, BigDecimal open, BigDecimal previousClose, Instant timestamp) {
}
