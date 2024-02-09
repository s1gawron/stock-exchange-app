package com.s1gawron.stockexchange.transaction.dao;

import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;

import java.util.List;

public interface TransactionDAO {

    void saveTransaction(Transaction transaction);

    List<Long> getNewTransactionIds();

    void changeTransactionsStatus(List<Long> transactionIds, TransactionStatus newTransactionStatus);

}
