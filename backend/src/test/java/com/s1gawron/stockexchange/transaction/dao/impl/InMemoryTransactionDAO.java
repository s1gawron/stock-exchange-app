package com.s1gawron.stockexchange.transaction.dao.impl;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryTransactionDAO implements TransactionDAO {

    private final List<Transaction> transactions = new ArrayList<>();

    @Override public void saveTransaction(final Transaction transaction) {
        ReflectionTestUtils.setField(transaction, "id", (long) transactions.size());
        transactions.add(transaction);
    }

    @Override public Optional<Transaction> getTransactionById(final long transactionId) {
        return transactions.stream()
            .filter(transaction -> transaction.getId() == transactionId)
            .findFirst();
    }

    @Override public void updateTransaction(final Transaction updatedTransaction) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId().equals(updatedTransaction.getId())) {
                transactions.set(i, updatedTransaction);
                break;
            }
        }
    }
}
