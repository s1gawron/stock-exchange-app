package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.transaction.model.Transaction;

import java.math.BigDecimal;

public interface TransactionProcessorStrategy {

    boolean canProcessTransaction(Transaction transaction, BigDecimal currentPrice);

    void processTransaction(Transaction transaction, BigDecimal currentPrice);

}
