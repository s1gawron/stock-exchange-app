package com.s1gawron.stockexchange.transaction.exception;

public class TransactionProcessingException extends RuntimeException {

    private TransactionProcessingException(final String message) {
        super(message);
    }

    public static TransactionProcessingException create() {
        throw new TransactionProcessingException("The transaction did not meet the conditions for processing.");
    }
}
