package com.s1gawron.stockexchange.transaction.exception;

import com.s1gawron.stockexchange.transaction.model.TransactionType;

public class WrongTransactionTypeForProcessingException extends RuntimeException {

    private WrongTransactionTypeForProcessingException(final String message) {
        super(message);
    }

    public static WrongTransactionTypeForProcessingException create(final TransactionType expected, final TransactionType actual) {
        return new WrongTransactionTypeForProcessingException("Transaction could not be processed! Expected transaction type: "
            + expected + ", but was: " + actual);
    }
}
