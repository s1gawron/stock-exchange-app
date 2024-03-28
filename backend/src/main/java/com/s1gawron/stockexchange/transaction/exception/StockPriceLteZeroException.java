package com.s1gawron.stockexchange.transaction.exception;

public class StockPriceLteZeroException extends RuntimeException {

    private StockPriceLteZeroException(final String message) {
        super(message);
    }

    public static StockPriceLteZeroException create() {
        return new StockPriceLteZeroException("Stock price cannot be less than or equal 0!");
    }
}
