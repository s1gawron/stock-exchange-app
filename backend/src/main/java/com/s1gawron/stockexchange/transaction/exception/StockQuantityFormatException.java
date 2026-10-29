package com.s1gawron.stockexchange.transaction.exception;

public class StockQuantityFormatException extends RuntimeException {

    private StockQuantityFormatException(final String message) {
        super(message);
    }

    public static StockQuantityFormatException createForDecimalPlaces(final int maxDecimalPlaces) {
        return new StockQuantityFormatException("Stock quantity cannot have more than " + maxDecimalPlaces + " decimal places!");
    }

    public static StockQuantityFormatException createForIntegerDigits(final int maxIntegerDigits) {
        return new StockQuantityFormatException("Stock quantity cannot have more than " + maxIntegerDigits + " integer digits!");
    }
}
