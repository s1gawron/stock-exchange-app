package pl.eizodev.app.services;

import org.springframework.stereotype.Service;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.repository.StockRepository;
import pl.eizodev.app.repository.UserRepository;
import pl.eizodev.app.stockstats.StockFactory;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    public StockServiceImpl(StockRepository stockRepository, UserRepository userRepository) {
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Stock findByUserAndStockTicker(User user, String ticker) {
        return stockRepository.findByUserAndTicker(user, ticker);
    }

    @Override
    public void saveStock(Stock stock) {
        stockRepository.save(stock);
    }

    @Override
    public void updateStock(String username) {
        List<Stock> userStocks = userRepository.findByName(username).getUserStock();

        if (!userStocks.isEmpty()) {
            StockFactory stockFactory = new StockFactory();
            List<Stock> WIG20 = stockFactory.getAllStocksFromGivenIndex("WIG20");
            List<Stock> WIG40 = stockFactory.getAllStocksFromGivenIndex("WIG40");
            List<Stock> WIG80 = stockFactory.getAllStocksFromGivenIndex("WIG80");

            for (Stock stock : userStocks) {
                Stock temp = null;

                switch (stock.getIndex()) {
                    case "WIG20":
                        temp = stockFactory.getByTicker(WIG20, stock.getTicker());
                        break;
                    case "WIG40":
                        temp = stockFactory.getByTicker(WIG40, stock.getTicker());
                        break;
                    case "WIG80":
                        temp = stockFactory.getByTicker(WIG80, stock.getTicker());
                        break;
                }

                stock.setPrice(temp.getPrice());
                stock.setChange(temp.getChange());
                stock.setProfitLoss((stock.getPrice() - stock.getAveragePurchasePrice()) * stock.getQuantity());
            }
        }
    }

    @Override
    public void deleteStock(Long id) {
        stockRepository.delete(stockRepository.findByStockId(id));
    }
}