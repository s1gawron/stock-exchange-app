package pl.eizodev.app.stock;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
@Deprecated(forRemoval = true)
public class StockService {

    private final StockRepository stockRepository;

    public void saveStock(final Stock stock) {
        stockRepository.save(stock);
    }

    public void deleteStock(final Long id) {
        final Optional<Stock> stockOptional = stockRepository.findByStockId(id);
        stockOptional.ifPresent(stockRepository::delete);
    }
}