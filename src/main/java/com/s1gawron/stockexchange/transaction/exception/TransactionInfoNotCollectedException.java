package com.s1gawron.stockexchange.transaction.exception;

public class TransactionInfoNotCollectedException extends RuntimeException {

    private TransactionInfoNotCollectedException(final String message) {
        super(message);
    }

    public static TransactionInfoNotCollectedException create() {
        return new TransactionInfoNotCollectedException("Transaction info has not been collected!");
    }
}
