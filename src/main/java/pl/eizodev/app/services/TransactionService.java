package pl.eizodev.app.services;

import org.springframework.stereotype.Service;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.repositories.TransactionRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public void performTransaction(String transactionType, String ticker, Long userId, BigDecimal price, int quantity) {
        Optional<Transaction> existInDB = transactionRepository.findByTransactionTypeAndStockTicker(transactionType, ticker);

        if (existInDB.isPresent()) {

        } else {
            Transaction transaction = new Transaction(transactionType, ticker, price, quantity, userId);
            addTransaction(transaction);
        }
    }

    public void deleteTransaction(Long id) {
        transactionRepository.delete(transactionRepository.findByTransactionId(id));
    }
}