package com.s1gawron.stockexchange.transaction.service.strategy;

public interface TransactionCreatorStrategy {

    boolean validateTransaction();

    void createTransaction();

}
