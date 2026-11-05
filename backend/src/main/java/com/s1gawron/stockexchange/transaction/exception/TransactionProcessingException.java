package com.s1gawron.stockexchange.transaction.exception;

public class TransactionProcessingException extends RuntimeException {

    private TransactionProcessingException(final String message) {
        super(message);
    }

    public static TransactionProcessingException createForUnavailablePrice(final String stockTicker) {
        return new TransactionProcessingException("Could not perform transaction because current price of " + stockTicker + " stock is not available.");
    }

    public static TransactionProcessingException createForUnavailableCurrency(final String stockTicker) {
        return new TransactionProcessingException("Could not perform transaction because currency of " + stockTicker + " stock is not available.");
    }
}
