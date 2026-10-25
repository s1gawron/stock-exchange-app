package com.s1gawron.stockexchange.stock;

import java.util.Locale;

public enum StockTickerNormalizer {

    I;

    public String normalize(final String ticker) {
        if (ticker == null) {
            return null;
        }

        return ticker.trim().toUpperCase(Locale.ROOT);
    }

}
