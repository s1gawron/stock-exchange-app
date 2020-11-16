package pl.eizodev.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.User;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByStockId(Long id);
    Optional<Stock> findByUserAndTicker(User user, String ticker);
}
