package com.s1gawron.stockexchange.transaction.matching;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One trade from the real market. Its price and volume decide which open transactions can be filled.
 */
public record Trade(String ticker, BigDecimal price, BigDecimal volume, Instant timestamp) {

}
