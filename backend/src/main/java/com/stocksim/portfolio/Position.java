package com.stocksim.portfolio;

import java.math.BigDecimal;

public record Position(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis) {
}
