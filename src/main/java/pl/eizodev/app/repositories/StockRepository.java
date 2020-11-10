package pl.eizodev.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.User;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByStockId(Long id);
    Stock findByUserAndTicker(User user, String ticker);
}
