package pl.eizodev.app.services;

import org.springframework.stereotype.Service;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.TransactionType;
import pl.eizodev.app.repositories.TransactionRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(final TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void addTransaction(final Transaction transaction) {
        transactionRepository.save(transaction);
    }

    //TO-DO rethink if it is a good idea to do it this way
    public void performTransaction(final TransactionType transactionType, final String ticker, final Long userId, final BigDecimal price,
                                   final int quantity) {
        final Optional<Transaction> existInDB = transactionRepository.findByTransactionTypeAndStockTicker(transactionType, ticker);

        if (existInDB.isPresent()) {

        } else {
//            final Transaction transaction = new Transaction(transactionType, ticker, price, quantity, userId);
//            addTransaction(transaction);
        }
    }

    public void deleteTransaction(final Long id) {
        transactionRepository.delete(transactionRepository.findByTransactionId(id));
    }
}