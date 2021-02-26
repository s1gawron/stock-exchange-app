package pl.eizodev.app.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.StockRepository;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.stockstats.StockFactory;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockFactory stockFactory;

    public void saveStock(final Stock stock) {
        stockRepository.save(stock);
    }

    public void updateStock(final String username) {
        final Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            final List<Stock> userStocks = user.getUserStock();

            for (final Stock stock : userStocks) {
                final Optional<Stock> temp = stockFactory.getByTicker(stock.getStockIndex(), stock.getTicker());

                if (temp.isPresent()) {
                    stock.setPrice(temp.get().getPrice());
                    stock.setPercentageChange(temp.get().getPercentageChange());
                    stock.setPriceChange(temp.get().getPriceChange());
                    stock.setProfitLoss((stock.getPrice().subtract(stock.getAveragePurchasePrice())).multiply(BigDecimal.valueOf(stock.getQuantity())));
                    stock.setLastUpdateDate(temp.get().getLastUpdateDate());
                }
            }
        }
    }

    public void deleteStock(final Long id) {
        final Optional<Stock> stockOptional = stockRepository.findByStockId(id);
        stockOptional.ifPresent(stockRepository::delete);
    }
}