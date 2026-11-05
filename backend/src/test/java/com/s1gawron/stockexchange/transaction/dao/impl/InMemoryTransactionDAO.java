package com.s1gawron.stockexchange.transaction.dao.impl;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution;
import com.s1gawron.stockexchange.transaction.model.TransactionStockBlock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InMemoryTransactionDAO implements TransactionDAO {

    private final List<Transaction> transactions = new ArrayList<>();

    private final List<TransactionExecution> transactionExecutions = new ArrayList<>();

    private final List<TransactionStockBlock> transactionStockBlocks = new ArrayList<>();

    private long nextTransactionStockBlockId = 1;

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

    @Override public List<Transaction> getTransactionsByWalletId(final long walletId) {
        return transactions.stream()
            .filter(transaction -> transaction.getWalletId() == walletId)
            .sorted(Comparator.comparing(Transaction::getTransactionCreateDate).thenComparing(Transaction::getId).reversed())
            .toList();
    }

    @Override public List<Transaction> getOpenTransactions() {
        return transactions.stream()
            .filter(Transaction::isOpen)
            .toList();
    }

    @Override public void saveTransactionExecution(final TransactionExecution transactionExecution) {
        ReflectionTestUtils.setField(transactionExecution, "id", (long) transactionExecutions.size() + 1);
        transactionExecutions.add(transactionExecution);
    }

    @Override public List<TransactionExecution> getTransactionExecutions(final Collection<Long> transactionIds) {
        return transactionExecutions.stream()
            .filter(execution -> transactionIds.contains(execution.getTransactionId()))
            .toList();
    }

    @Override public void saveTransactionStockBlock(final TransactionStockBlock transactionStockBlock) {
        ReflectionTestUtils.setField(transactionStockBlock, "id", nextTransactionStockBlockId++);
        transactionStockBlocks.add(transactionStockBlock);
    }

    @Override public List<TransactionStockBlock> getTransactionStockBlocks(final long transactionId) {
        return transactionStockBlocks.stream()
            .filter(block -> block.getTransactionId() == transactionId)
            .sorted(Comparator.comparing(TransactionStockBlock::getId))
            .toList();
    }

    @Override public void updateTransactionStockBlock(final TransactionStockBlock transactionStockBlock) {
        // Entities are kept by reference, so there is nothing to update.
    }

    @Override public void deleteTransactionStockBlock(final TransactionStockBlock transactionStockBlock) {
        transactionStockBlocks.removeIf(block -> block.getId().equals(transactionStockBlock.getId()));
    }

    public List<TransactionExecution> getAllTransactionExecutions() {
        return List.copyOf(transactionExecutions);
    }

    public List<TransactionStockBlock> getAllTransactionStockBlocks() {
        return List.copyOf(transactionStockBlocks);
    }
}
