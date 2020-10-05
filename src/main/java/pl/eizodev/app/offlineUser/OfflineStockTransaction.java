package pl.eizodev.app.offlineUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.webScrape.StocksStats;

import javax.transaction.Transactional;

@Transactional
@Service
public class OfflineStockTransaction {

    @Autowired
    private UserService userService;

    @Autowired
    private StockService stockService;

    private void stockPurchase(int quantity, String ticker, Long userId) {
        User user = userService.findById(userId).get();
        Stock stock = stockService.findByUserAndStockTicker(user, ticker);
        StocksStats stocksStats = new StocksStats();

        Stock newStock = stocksStats.getByTicker(stocksStats.getAllStocksWIG20(), ticker);

        if (stock != null) {
            stock.setAveragePurchasePrice(((stock.getQuantity() * stock.getPrice()) + (quantity * stocksStats.getByTicker(stocksStats.getAllStocksWIG20(), ticker).getPrice())) / (stock.getQuantity() + quantity));
            stock.setQuantity(stock.getQuantity() + quantity);
        } else {
            newStock.setQuantity(quantity);
            newStock.setAveragePurchasePrice(newStock.getPrice());
            newStock.setUser(user);
            user.getUserStock().add(newStock);
            stockService.saveStock(newStock);
        }
        user.setBalanceAvailable(user.getBalanceAvailable() - (quantity * newStock.getPrice()));
    }

    private void stockSell(int quantity, String ticker, Long userId) {
        User user = userService.findById(userId).get();
        Stock stock = stockService.findByUserAndStockTicker(user, ticker);

        StocksStats stocksStats = new StocksStats();
        user.setBalanceAvailable(user.getBalanceAvailable() + (quantity * stocksStats.getByTicker(stocksStats.getAllStocksWIG20(), ticker).getPrice()));

        if (stock.getQuantity() == quantity) {
            stockService.deleteStock(stock.getStockId());
        } else {
            stock.setQuantity(stock.getQuantity() - quantity);
        }
    }

    public void performTransaction(Transaction transaction) {
        int quantity = transaction.getStockQuantity();
        String ticker = transaction.getStockTicker();
        Long userId = transaction.getUserId();

        if (transaction.getTransactionType().equals("buy")) {
            stockPurchase(quantity, ticker, userId);
        } else if (transaction.getTransactionType().equals("sell")) {
            stockSell(quantity, ticker, userId);
        }
    }
}