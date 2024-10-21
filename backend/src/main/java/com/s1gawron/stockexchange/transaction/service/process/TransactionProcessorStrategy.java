package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.transaction.model.Transaction;

public interface TransactionProcessorStrategy {

    boolean canProcessTransaction(Transaction transaction);

    void processTransaction(Transaction transaction);

    default boolean cannotProcessTransaction(Transaction transaction) {
        return !canProcessTransaction(transaction);
    }

}
