package pl.eizodev.app.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.eizodev.app.user.User;

import java.util.Optional;

@Repository
@Deprecated(forRemoval = true)
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByStockId(Long id);

    Optional<Stock> findByUserAndTicker(User user, String ticker);
}
