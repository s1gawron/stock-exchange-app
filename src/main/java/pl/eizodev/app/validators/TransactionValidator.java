package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stocks.StockWIG20;

import java.util.List;
import java.util.Optional;

public class TransactionValidator {

    final UserService userService;

    public TransactionValidator(UserService userService) {
        this.userService = userService;
    }

    public void hasEnoughMoney(Transaction transaction, Errors errors) {
        StockWIG20 stockWIG20 = new StockWIG20();
        Optional<User> userOptional = userService.findById(transaction.getUserId());
        User user = userOptional.get();
        int quantity = transaction.getStockQuantity();
        String ticker = transaction.getStockTicker();
        float price = stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker).getPrice();
        float transactionCost = quantity * price;

        int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker).getPrice())));

        if (user.getBalanceAvailable() < transactionCost) {
            errors.rejectValue("stockQuantity", "error.notEnoughMoney");
        }
    }

    public void hasEnoughStock(Transaction transaction, Errors errors) {
        Optional<User> userOptional = userService.findById(transaction.getUserId());
        User user = userOptional.get();
        List<Stock> userStock = user.getUserStock();
        String ticker = transaction.getStockTicker();
        int quantity = transaction.getStockQuantity();

        Optional<Integer> amountOfStock = userStock.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .map(Stock::getQuantity)
                .findFirst();

        if (!amountOfStock.isPresent()) {
            errors.rejectValue("stockQuantity", "error.noSuchStock");
        } else if (amountOfStock.get() < quantity) {
            errors.rejectValue("stockQuantity", "error.notEnoughStock");
        }
    }
}