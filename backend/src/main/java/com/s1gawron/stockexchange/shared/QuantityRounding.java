package com.s1gawron.stockexchange.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum QuantityRounding {

    I;

    public static final int QUANTITY_SCALE = 6;

    public BigDecimal quantity(final BigDecimal value) {
        return value.setScale(QUANTITY_SCALE, RoundingMode.DOWN);
    }

}
