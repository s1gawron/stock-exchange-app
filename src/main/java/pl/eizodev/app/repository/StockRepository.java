package pl.eizodev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByStockId(Long id);
    Optional<Stock> findByUserAndTicker(User user, String ticker);
}
