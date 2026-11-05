package com.s1gawron.stockexchange.transaction.dao;

import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution;
import com.s1gawron.stockexchange.transaction.model.TransactionStockBlock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TransactionDAO {

    void saveTransaction(Transaction transaction);

    Optional<Transaction> getTransactionById(long transactionId);

    void updateTransaction(Transaction transaction);

    /**
     * Returns all transactions of the wallet, newest first.
     */
    List<Transaction> getTransactionsByWalletId(long walletId);

    /**
     * Returns all transactions with status NEW or PARTIALLY_FILLED.
     */
    List<Transaction> getOpenTransactions();

    void saveTransactionExecution(TransactionExecution transactionExecution);

    /**
     * Returns the executions of the given transactions, oldest first.
     */
    List<TransactionExecution> getTransactionExecutions(Collection<Long> transactionIds);

    void saveTransactionStockBlock(TransactionStockBlock transactionStockBlock);

    /**
     * Returns the stock blocks of the transaction in the order in which they were created.
     */
    List<TransactionStockBlock> getTransactionStockBlocks(long transactionId);

    void updateTransactionStockBlock(TransactionStockBlock transactionStockBlock);

    void deleteTransactionStockBlock(TransactionStockBlock transactionStockBlock);

}
