package com.s1gawron.stockexchange.transaction.service.strategy;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;

public interface TransactionCreatorStrategy {

    void collectTransactionInfo(TransactionRequestDTO transactionRequestDTO);

    boolean validateTransaction();

    void createTransaction();

    TransactionRequestDTO getTransactionInfo();

}
