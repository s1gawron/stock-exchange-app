package pl.eizodev.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stocks.StockWIG20;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class StockActions {

    @Autowired
    private UserService userService;

    @Autowired
    private StockService stockService;

    private static boolean containsStock(final List<Stock> list, final String ticker) {
        return list.stream().anyMatch(o -> o.getTicker().equals(ticker));
    }

    public void stockPurchase(int quantity, String ticker, Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        User user = userOptional.get();
        List<Stock> userStock = user.getUserStock();
        StockWIG20 stockWIG20 = new StockWIG20();

        if (user.getBalanceAvailable() >= quantity * stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker).getPrice() && quantity > 0) {
            Optional<Long> stockId = userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .map(Stock::getStockId)
                    .findFirst();
            Stock stock = new Stock();

            if (stockId.isPresent()) {
                stock = stockService.findById(stockId.get());
            }

            Stock newStock = stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker);

            if (containsStock(userStock, ticker)) {
                stock.setAveragePurchasePrice(((stock.getQuantity() * stock.getPrice()) + (quantity * stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker).getPrice())) / (stock.getQuantity() + quantity));
                stock.setQuantity(stock.getQuantity() + quantity);
            } else {
                newStock.setQuantity(quantity);
                newStock.setAveragePurchasePrice(newStock.getPrice());
                newStock.setUser(user);
                user.getUserStock().add(newStock);
                stockService.saveStock(newStock);
            }
            user.setBalanceAvailable(user.getBalanceAvailable() - (quantity * newStock.getPrice()));
            System.out.println("Transakcja przebiegla pomyslnie.");
        } else {
            int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker).getPrice())));
            System.out.println("Nie masz odpowiednich srodkow, maksymalna ilosc akcji jakie mozesz kupic: " + maxAmount);
        }
    }

    public void stockSell(int quantity, String ticker, Long userId) {
        Optional<User> userOptional = userService.findById(userId);
        User user = userOptional.get();
        List<Stock> userStock = user.getUserStock();
        Optional<Integer> amountOfStock = userStock.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .map(Stock::getQuantity)
                .findFirst();

        if (quantity > 0 && amountOfStock.isPresent()) {
            if (amountOfStock.get() >= quantity) {
                Optional<Long> stockId = userStock.stream()
                        .filter(o -> o.getTicker().equals(ticker))
                        .map(Stock::getStockId)
                        .findFirst();

                Stock stock = stockService.findById(stockId.get());
                stock.setQuantity(stock.getQuantity() - quantity);
                StockWIG20 stockWIG20 = new StockWIG20();
                user.setBalanceAvailable(user.getBalanceAvailable() + (quantity * stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker).getPrice()));

                if (amountOfStock.get() == quantity) {
                    stockService.deleteStock(stock.getStockId());
                }
                System.out.println("Transakcja przebiegla pomyslnie.");
            } else {
                System.out.println("Nie posiadasz takiej ilosci akcji! Ilosc akcji w Twoim portfelu: " + amountOfStock);
            }
        } else {
            System.out.println("Nie posiadasz tych akcji!");
        }
    }
}