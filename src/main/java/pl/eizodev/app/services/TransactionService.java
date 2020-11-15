package pl.eizodev.app.services;

import pl.eizodev.app.entities.Transaction;

import java.math.BigDecimal;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    void performTransaction(String transactionType, String ticker, Long userId, BigDecimal price, int quantity);
    void deleteTransaction(Long id);
}
