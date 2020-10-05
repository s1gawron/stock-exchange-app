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
        StocksStats stocksStats = new StocksStats();
        List<Stock> stockList = stocksStats.getAllStocksWIG20();

        if (!userStocks.isEmpty()) {
            for (Stock stock : userStocks) {
                Stock temp = stocksStats.getByTicker(stockList, stock.getTicker());

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