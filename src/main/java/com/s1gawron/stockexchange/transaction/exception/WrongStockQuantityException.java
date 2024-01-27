package com.s1gawron.stockexchange.transaction.exception;

public class WrongStockQuantityException extends RuntimeException {

    private WrongStockQuantityException(final String message) {
        super(message);
    }

    public static WrongStockQuantityException create() {
        return new WrongStockQuantityException("Stock quantity cannot be less than or equal to 0!");
    }
}
