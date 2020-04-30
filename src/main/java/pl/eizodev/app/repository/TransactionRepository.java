package pl.eizodev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.eizodev.app.entity.Transaction;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findByTransactionId(Long id);
    Optional<Transaction> findByTransactionTypeAndStockTicker(String transactionType, String ticker);
}