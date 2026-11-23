package com.stocksim.account;

import java.math.BigDecimal;

/**
 * Snapshot of an account's cash. {@code reservedCash} is held by OPEN orders; {@link #availableCash()} is what new
 * orders may use. Money decisions must use a value read under the account lock ({@link AccountService#lock}).
 */
public record AccountBalance(long id, long userId, String currency, BigDecimal cashBalance,
		BigDecimal reservedCash) {

	public BigDecimal availableCash() {
		return cashBalance.subtract(reservedCash);
	}
}
