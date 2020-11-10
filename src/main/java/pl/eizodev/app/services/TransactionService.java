package pl.eizodev.app.services;

import pl.eizodev.app.entities.Transaction;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    void performTransaction(String transactionType, String ticker, Long userId, float price, int quantity);
    void deleteTransaction(Long id);
}
