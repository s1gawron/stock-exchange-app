package pl.eizodev.app.offlineuser;

import org.springframework.stereotype.Service;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.StockRepository;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.stockstats.StockFactory;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Transactional
@Service
public class OfflineStockTransaction {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    public OfflineStockTransaction(StockRepository stockRepository, UserRepository userRepository, StockService stockService) {
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
    }

    private void stockPurchase(int quantity, String index, String ticker, Long userId) {
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            Optional<Stock> stockOptional = stockRepository.findByUserAndTicker(user, ticker);
            StockFactory stockFactory = new StockFactory();

            Stock newStock = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();
                stock.setAveragePurchasePrice(((stock.getPrice().multiply(BigDecimal.valueOf(stock.getQuantity()))).add((stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker).getPrice()).multiply(BigDecimal.valueOf(quantity)))).divide(BigDecimal.valueOf(stock.getQuantity() + quantity), RoundingMode.UNNECESSARY));
                stock.setQuantity(stock.getQuantity() + quantity);
            } else {
                newStock.setQuantity(quantity);
                newStock.setAveragePurchasePrice(newStock.getPrice());
                newStock.setUser(user);
                user.getUserStock().add(newStock);
                stockService.saveStock(newStock);
            }
            user.setBalanceAvailable(user.getBalanceAvailable().subtract(newStock.getPrice().multiply(BigDecimal.valueOf(quantity))));
        }
    }

    private void stockSell(int quantity, String index, String ticker, Long userId) {
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<Stock> stockOptional = stockRepository.findByUserAndTicker(user, ticker);

            StockFactory stockFactory = new StockFactory();
            user.setBalanceAvailable(user.getBalanceAvailable().add(stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker).getPrice().multiply(BigDecimal.valueOf(quantity))));

            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();

                if (stock.getQuantity() == quantity) {
                    stockService.deleteStock(stock.getStockId());
                } else {
                    stock.setQuantity(stock.getQuantity() - quantity);
                }
            }
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