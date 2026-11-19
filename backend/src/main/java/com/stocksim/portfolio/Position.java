package com.stocksim.portfolio;

import java.math.BigDecimal;

/** {@code reservedQty} is held by OPEN sell orders. */
public record Position(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis, BigDecimal reservedQty) {

	public BigDecimal availableQty() {
		return qty.subtract(reservedQty);
	}
}
