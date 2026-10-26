package com.s1gawron.stockexchange.transaction.exception;

public class StockPriceFormatException extends RuntimeException {

    private StockPriceFormatException(final String message) {
        super(message);
    }

    public static StockPriceFormatException createForDecimalPlaces(final int maxDecimalPlaces) {
        return new StockPriceFormatException("Stock price cannot have more than " + maxDecimalPlaces + " decimal places!");
    }

    public static StockPriceFormatException createForIntegerDigits(final int maxIntegerDigits) {
        return new StockPriceFormatException("Stock price cannot have more than " + maxIntegerDigits + " integer digits!");
    }
}
