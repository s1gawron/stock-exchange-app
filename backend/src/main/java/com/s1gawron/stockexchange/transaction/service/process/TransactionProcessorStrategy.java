package com.s1gawron.stockexchange.transaction.service.process;

public interface TransactionProcessorStrategy {

    boolean canProcessTransaction();

    void processTransaction();

    default boolean cannotProcessTransaction() {
        return !canProcessTransaction();
    }

}
