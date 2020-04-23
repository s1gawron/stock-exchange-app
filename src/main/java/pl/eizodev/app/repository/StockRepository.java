package pl.eizodev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByStockId(Long id);
    Stock findByUserAndTicker(User user, String ticker);
}
