package pl.eizodev.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.TransactionType;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findByTransactionId(Long id);
    Optional<Transaction> findByTransactionTypeAndStockTicker(TransactionType transactionType, String ticker);
}