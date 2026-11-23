package com.stocksim.portfolio;

import java.math.BigDecimal;

/**
 * Shares of one symbol held by an account. {@code reservedQty} is held by OPEN sell orders. Money decisions must use
 * a value read inside the locking transaction, never one read earlier.
 */
public record PositionHolding(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis,
		BigDecimal reservedQty) {

	public BigDecimal availableQty() {
		return qty.subtract(reservedQty);
	}
}
