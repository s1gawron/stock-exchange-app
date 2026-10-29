package com.s1gawron.stockexchange.transaction.exception;

public class PositionNotAllowedForPurchaseException extends RuntimeException {

    private PositionNotAllowedForPurchaseException(final String message) {
        super(message);
    }

    public static PositionNotAllowedForPurchaseException create() {
        return new PositionNotAllowedForPurchaseException("Position can be selected only for sell transaction!");
    }
}
