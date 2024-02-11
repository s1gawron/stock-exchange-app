package com.s1gawron.stockexchange.transaction.service.process;

public interface TransactionProcessorStrategy {

    boolean checkStockPrice();

    void processTransaction();

}
