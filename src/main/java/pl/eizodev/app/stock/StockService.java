package pl.eizodev.app.stock;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.eizodev.app.user.User;
import pl.eizodev.app.user.UserRepository;

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

    public void updateUserStockByUsername(final String username) {
        final Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            final List<Stock> userStocks = user.getUserStock();

            for (final Stock stock : userStocks) {
                final Stock tempStock = stockFactory.getByTicker(stock.getStockIndex(), stock.getTicker());

                stock.setPrice(tempStock.getPrice());
                stock.setPercentageChange(tempStock.getPercentageChange());
                stock.setPriceChange(tempStock.getPriceChange());
                stock.setProfitLoss((stock.getPrice().subtract(stock.getAveragePurchasePrice())).multiply(BigDecimal.valueOf(stock.getQuantity())));
                stock.setLastUpdateDate(tempStock.getLastUpdateDate());
            }
        }
    }

    public void deleteStock(final Long id) {
        final Optional<Stock> stockOptional = stockRepository.findByStockId(id);
        stockOptional.ifPresent(stockRepository::delete);
    }
}