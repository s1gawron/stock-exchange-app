package pl.eizodev.app.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.eizodev.app.stock.model.StockCompanyDetails;

import java.util.Optional;

public interface StockCompanyDetailsRepository extends JpaRepository<StockCompanyDetails, Long> {

    Optional<StockCompanyDetails> findByTicker(String ticker);
}
