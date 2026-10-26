package com.s1gawron.stockexchange.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum MoneyRounding {

    I;

    public static final int MONEY_SCALE = 4;

    public BigDecimal money(final BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

}
