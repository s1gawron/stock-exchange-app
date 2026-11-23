package com.stocksim.portfolio;

import java.math.BigDecimal;

/** Row of the {@code positions} table. Stays inside this module; other modules get {@link PositionHolding}. */
record PositionRow(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis, BigDecimal reservedQty) {
}
