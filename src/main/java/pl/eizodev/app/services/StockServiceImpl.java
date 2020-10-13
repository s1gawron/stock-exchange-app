package pl.eizodev.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.repository.StockRepository;
import pl.eizodev.app.repository.UserRepository;
import pl.eizodev.app.webScrape.StocksStats;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Stock findById(Long id) {
        return stockRepository.findByStockId(id);
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
            StocksStats stocksStats = new StocksStats();
            List<Stock> WIG20 = stocksStats.getAllStocksFromGivenIndex("WIG20");
            List<Stock> WIG40 = stocksStats.getAllStocksFromGivenIndex("WIG40");
            List<Stock> WIG80 = stocksStats.getAllStocksFromGivenIndex("WIG80");

            for (Stock stock : userStocks) {
                Stock temp = null;

                switch (stock.getIndex()) {
                    case "WIG20":
                        temp = stocksStats.getByTicker(WIG20, stock.getTicker());
                        break;
                    case "WIG40":
                        temp = stocksStats.getByTicker(WIG40, stock.getTicker());
                        break;
                    case "WIG80":
                        temp = stocksStats.getByTicker(WIG80, stock.getTicker());
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