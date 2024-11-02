package com.s1gawron.stockexchange.transaction.service.create;

public interface TransactionCreatorStrategy {

    boolean canCreateTransaction();

    void createTransaction();

}
