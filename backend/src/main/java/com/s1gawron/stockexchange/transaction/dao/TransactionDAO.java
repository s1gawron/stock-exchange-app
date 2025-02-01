package com.s1gawron.stockexchange.transaction.dao;

import com.s1gawron.stockexchange.transaction.model.Transaction;

import java.util.Optional;

public interface TransactionDAO {

    void saveTransaction(Transaction transaction);

    Optional<Transaction> getTransactionById(long transactionId);

    void updateTransaction(Transaction transaction);

}
