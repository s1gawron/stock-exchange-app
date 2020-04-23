package pl.eizodev.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.TransactionStock;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.repository.TransactionRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    @Override
    public void performTransaction(String transactionType, String ticker, User user, float price, int quantity) {
        Optional<Transaction> existInDB = transactionRepository.findByTransactionTypeAndTransactionStockTicker(transactionType, ticker);

        if (existInDB.isPresent()) {

        } else {
            TransactionStock transactionStock = new TransactionStock(ticker, price, quantity);
            Transaction transaction = new Transaction(transactionType, user, transactionStock);
            addTransaction(transaction);
        }
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionRepository.delete(transactionRepository.findByTransactionId(id));
    }
}