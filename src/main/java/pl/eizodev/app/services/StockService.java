package pl.eizodev.app.services;

import org.springframework.stereotype.Service;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.StockIndex;
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
public class StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    public StockService(StockRepository stockRepository, UserRepository userRepository) {
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
    }

    public void saveStock(Stock stock) {
        stockRepository.save(stock);
    }

    public void updateStock(String username) {
        Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Stock> userStocks = user.getUserStock();

            if (!userStocks.isEmpty()) {
                StockFactory stockFactory = new StockFactory();
                List<Stock> WIG20 = stockFactory.getAllStocksFromGivenIndex(StockIndex.WIG20);
                List<Stock> WIG40 = stockFactory.getAllStocksFromGivenIndex(StockIndex.WIG40);
                List<Stock> WIG80 = stockFactory.getAllStocksFromGivenIndex(StockIndex.WIG80);

                for (Stock stock : userStocks) {
                    Optional<Stock> temp = Optional.empty();

                    switch (stock.getStockIndex()) {
                        case WIG20:
                            temp = stockFactory.getByTicker(WIG20, stock.getTicker());
                            break;
                        case WIG40:
                            temp = stockFactory.getByTicker(WIG40, stock.getTicker());
                            break;
                        case WIG80:
                            temp = stockFactory.getByTicker(WIG80, stock.getTicker());
                            break;
                    }

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
    }

    public void deleteStock(Long id) {
        Optional<Stock> stockOptional = stockRepository.findByStockId(id);
        stockOptional.ifPresent(stockRepository::delete);
    }
}