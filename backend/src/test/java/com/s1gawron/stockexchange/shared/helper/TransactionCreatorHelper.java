package com.s1gawron.stockexchange.shared.helper;

import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionPosition;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public enum TransactionCreatorHelper {

    I;

    public Transaction createAppleStockTransactionWithBalanceBlocked(final TransactionType transactionType) {
        return createAppleStockTransactionWithBalanceBlocked(transactionType, 10, new BigDecimal("25.00"));
    }

    public Transaction createAppleStockTransactionWithBalanceBlocked(final TransactionType transactionType, final int quantity,
        final BigDecimal purchasePrice) {
        final TransactionPosition transactionPosition = new TransactionPosition("AAPL", purchasePrice, quantity);
        final Transaction transaction = new Transaction(1, transactionType, TransactionStatus.NEW, LocalDateTime.now(), transactionPosition);

        final BigDecimal balanceBlocked = BigDecimal.valueOf(quantity).multiply(purchasePrice);
        transaction.setBalanceBlocked(balanceBlocked);

        return transaction;
    }

    public Transaction createAppleStockTransaction(final TransactionType transactionType) {
        return createAppleStockTransaction(transactionType, 10, new BigDecimal("25.00"));
    }

    public Transaction createAppleStockTransaction(final TransactionType transactionType, final int quantity, final BigDecimal purchasePrice) {
        final TransactionPosition transactionPosition = new TransactionPosition("AAPL", purchasePrice, quantity);
        return new Transaction(1, transactionType, TransactionStatus.NEW, LocalDateTime.now(), transactionPosition);
    }

}
