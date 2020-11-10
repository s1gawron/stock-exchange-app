package pl.eizodev.app.offlineUser;

import org.springframework.stereotype.Service;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stockstats.StockFactory;

import javax.transaction.Transactional;

@Transactional
@Service
public class OfflineStockTransaction {

    private final UserService userService;
    private final StockService stockService;

    public OfflineStockTransaction(UserService userService, StockService stockService) {
        this.userService = userService;
        this.stockService = stockService;
    }

    private void stockPurchase(int quantity, String index, String ticker, Long userId) {
        User user = userService.findById(userId).get();
        Stock stock = stockService.findByUserAndStockTicker(user, ticker);
        StockFactory stockFactory = new StockFactory();

        Stock newStock = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

        if (stock != null) {
            stock.setAveragePurchasePrice(((stock.getQuantity() * stock.getPrice()) + (quantity * stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker).getPrice())) / (stock.getQuantity() + quantity));
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

    private void stockSell(int quantity, String index, String ticker, Long userId) {
        User user = userService.findById(userId).get();
        Stock stock = stockService.findByUserAndStockTicker(user, ticker);

        StockFactory stockFactory = new StockFactory();
        user.setBalanceAvailable(user.getBalanceAvailable() + (quantity * stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker).getPrice()));

        if (stock.getQuantity() == quantity) {
            stockService.deleteStock(stock.getStockId());
        } else {
            stock.setQuantity(stock.getQuantity() - quantity);
        }
    }

    public void performTransaction(Transaction transaction) {
        int quantity = transaction.getStockQuantity();
        String index = transaction.getStockIndex();
        String ticker = transaction.getStockTicker();
        Long userId = transaction.getUserId();

        if ("buy".equals(transaction.getTransactionType())) {
            stockPurchase(quantity, index, ticker, userId);
        } else if ("sell".equals(transaction.getTransactionType())) {
            stockSell(quantity, index, ticker, userId);
        }
    }
}