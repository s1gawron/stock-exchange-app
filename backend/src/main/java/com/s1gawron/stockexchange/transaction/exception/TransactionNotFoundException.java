package com.s1gawron.stockexchange.transaction.exception;

public class TransactionNotFoundException extends RuntimeException {

    private TransactionNotFoundException(final String message) {
        super(message);
    }

    public static TransactionNotFoundException create(final long transactionId) {
        return new TransactionNotFoundException("Transaction#" + transactionId + " could not be found!");
    }
}
