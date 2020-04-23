package pl.eizodev.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.repository.StockRepository;
import pl.eizodev.app.stocks.StockWIG20;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepository;

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
    public void updateStock(User user) {
        List<Stock> userStocks = user.getUserStock();
        StockWIG20 stockWIG20 = new StockWIG20();

        for (Stock stock : userStocks) {
            stock.setPrice(stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), stock.getTicker()).getPrice());
            stock.setChange(stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), stock.getTicker()).getChange());
            stock.setProfitLoss((stock.getPrice() - stock.getAveragePurchasePrice()) * stock.getQuantity());
        }
    }

    @Override
    public void deleteStock(Long id) {
        stockRepository.delete(stockRepository.findByStockId(id));
    }
}