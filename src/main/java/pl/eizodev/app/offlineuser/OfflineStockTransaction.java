package pl.eizodev.app.offlineuser;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.StockIndex;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.StockRepository;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.stockstats.StockFactory;
import pl.eizodev.app.validators.TransactionValidator;

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

    public BindingResult canPerformTransaction(Transaction transaction, BindingResult result) {
        new TransactionValidator(userRepository).validate(transaction, result);
        return result;
    }

    public void performTransaction(Transaction transaction) {
        int quantity = transaction.getStockQuantity();
        StockIndex index = transaction.getStockIndex();
        String ticker = transaction.getStockTicker();
        Long userId = transaction.getUserId();

        if ("buy".equals(transaction.getTransactionType())) {
            stockPurchase(quantity, index, ticker, userId);
        } else if ("sell".equals(transaction.getTransactionType())) {
            stockSell(quantity, index, ticker, userId);
        }
    }

    private void stockPurchase(int quantity, StockIndex index, String ticker, Long userId) {
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            Optional<Stock> stockOptional = stockRepository.findByUserAndTicker(user, ticker);
            StockFactory stockFactory = new StockFactory();

            Optional<Stock> newStockOptional = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

            if (stockOptional.isPresent()) {
                Stock stock = stockOptional.get();
                Optional<Stock> stockTempOptional = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

                if (stockTempOptional.isPresent()) {
                    BigDecimal priceOfStock = stockTempOptional.get().getPrice();
                    BigDecimal denominator = (stock.getPrice().multiply(BigDecimal.valueOf(stock.getQuantity()))).add(priceOfStock.multiply(BigDecimal.valueOf(quantity)));
                    BigDecimal divider = BigDecimal.valueOf(stock.getQuantity() + quantity);
                    BigDecimal resultOfDivision = denominator.divide(divider, RoundingMode.UNNECESSARY);
                    stock.setAveragePurchasePrice(resultOfDivision);
                }
                stock.setQuantity(stock.getQuantity() + quantity);
            } else {
                newStockOptional.ifPresent(newStock -> {
                    newStock.setQuantity(quantity);
                    newStock.setAveragePurchasePrice(newStock.getPrice());
                    newStock.setUser(user);
                    user.getUserStock().add(newStock);
                    stockService.saveStock(newStock);
                });
            }
            newStockOptional.ifPresent(newStock -> user.setBalanceAvailable(user.getBalanceAvailable().subtract(newStock.getPrice().multiply(BigDecimal.valueOf(quantity)))));
        }
    }

    private void stockSell(int quantity, StockIndex index, String ticker, Long userId) {
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<Stock> stockOptional = stockRepository.findByUserAndTicker(user, ticker);

            StockFactory stockFactory = new StockFactory();
            Optional<Stock> stockTempOptional = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

            if (stockTempOptional.isPresent()) {
                BigDecimal priceOfStock = stockTempOptional.get().getPrice();
                user.setBalanceAvailable(user.getBalanceAvailable().add(priceOfStock.multiply(BigDecimal.valueOf(quantity))));
            }

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
}