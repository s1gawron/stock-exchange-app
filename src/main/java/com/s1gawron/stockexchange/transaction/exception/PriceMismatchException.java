package com.s1gawron.stockexchange.transaction.exception;

public class PriceMismatchException extends RuntimeException {

    private PriceMismatchException(final String message) {
        super(message);
    }

    public static PriceMismatchException createForPurchase() {
        return new PriceMismatchException("The current stock price exceeds the target price for this transaction!");
    }
}
