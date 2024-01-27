package com.s1gawron.stockexchange.transaction.dao;

import com.s1gawron.stockexchange.transaction.model.Transaction;

public interface TransactionDAO {

    void saveTransaction(Transaction transaction);

}
