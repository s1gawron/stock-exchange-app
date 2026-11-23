package com.stocksim.account;

import java.math.BigDecimal;

/** Row of the {@code accounts} table. Stays inside this module; other modules get {@link AccountBalance}. */
record AccountRow(long id, long userId, String currency, BigDecimal cashBalance, BigDecimal reservedCash) {
}
