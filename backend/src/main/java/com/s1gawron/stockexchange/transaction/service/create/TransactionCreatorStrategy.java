package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;

public interface TransactionCreatorStrategy {

    boolean canCreateTransaction(TransactionRequestDTO transactionRequestDTO);

    void createTransaction(TransactionRequestDTO transactionRequestDTO);

}
