package com.s1gawron.stockexchange.transaction.exception;

public class StockPurchasePriceLessThanZeroException extends RuntimeException {

    private StockPurchasePriceLessThanZeroException(final String message) {
        super(message);
    }

    public static StockPurchasePriceLessThanZeroException create() {
        return new StockPurchasePriceLessThanZeroException("Stock purchase price cannot be equal or less than 0!");
    }
}
