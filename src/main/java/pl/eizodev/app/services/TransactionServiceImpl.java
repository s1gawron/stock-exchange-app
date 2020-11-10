package pl.eizodev.app.services;

import org.springframework.stereotype.Service;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.repositories.TransactionRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public void performTransaction(String transactionType, String ticker, Long userId, float price, int quantity) {
        Optional<Transaction> existInDB = transactionRepository.findByTransactionTypeAndStockTicker(transactionType, ticker);

        if (existInDB.isPresent()) {

        } else {
            Transaction transaction = new Transaction(transactionType, ticker, price, quantity, userId);
            addTransaction(transaction);
        }
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionRepository.delete(transactionRepository.findByTransactionId(id));
    }
}