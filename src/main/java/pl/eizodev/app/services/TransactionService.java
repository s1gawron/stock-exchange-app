package pl.eizodev.app.services;

import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;

public interface TransactionService {
    void addTransaction(Transaction transaction);
    void performTransaction(String transactionType, String ticker, User user, float price, int quantity);
    void deleteTransaction(Long id);
}
