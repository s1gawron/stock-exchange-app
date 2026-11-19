package com.stocksim.account;

import java.math.BigDecimal;

/** {@code reservedCash} is held by OPEN orders; {@link #availableCash()} is what new orders may use. */
public record Account(long id, long userId, String currency, BigDecimal cashBalance, BigDecimal reservedCash) {

	public BigDecimal availableCash() {
		return cashBalance.subtract(reservedCash);
	}
}
