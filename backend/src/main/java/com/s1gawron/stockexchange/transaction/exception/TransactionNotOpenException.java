package com.s1gawron.stockexchange.transaction.exception;

import com.s1gawron.stockexchange.transaction.model.TransactionStatus;

public class TransactionNotOpenException extends RuntimeException {

    private TransactionNotOpenException(final String message) {
        super(message);
    }

    public static TransactionNotOpenException create(final Long transactionId, final TransactionStatus transactionStatus) {
        return new TransactionNotOpenException("Transaction#" + transactionId + " has status " + transactionStatus + " and cannot be changed anymore!");
    }
}
