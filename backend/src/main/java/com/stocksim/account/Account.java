package com.stocksim.account;

import java.math.BigDecimal;

public record Account(long id, long userId, String currency, BigDecimal cashBalance) {
}
