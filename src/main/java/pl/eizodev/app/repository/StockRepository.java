package pl.eizodev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.eizodev.app.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findByStockId(Long id);
}
