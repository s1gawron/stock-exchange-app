package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.stocks.StockWIG20;

import java.util.List;
import java.util.Optional;

public class TransactionValidator {

    public void hasEnoughMoney(Transaction transaction, Errors errors) {
        User user = transaction.getUser();
        float price = transaction.getStockPrice();
        int quantity = transaction.getStockQuantity();
        String ticker = transaction.getStockTicker();

        float transactionCost = quantity * price;
        StockWIG20 stockWIG20 = new StockWIG20();
        int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker).getPrice())));

        if (user.getBalanceAvailable() < transactionCost) {
            errors.rejectValue("quantity", "Nie masz odpowiednich srodkow, maksymalna ilosc akcji jakie mozesz kupic: " + maxAmount);
        }
    }

    public void hasEnoughStock(Transaction transaction, Errors errors) {
        User user = transaction.getUser();
        List<Stock> userStock = user.getUserStock();
        String ticker = transaction.getStockTicker();
        int quantity = transaction.getStockQuantity();

        Optional<Integer> amountOfStock = userStock.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .map(Stock::getQuantity)
                .findFirst();

        if (!amountOfStock.isPresent()) {
            errors.rejectValue("quantity", "error.noSuchStock");
        } else if (amountOfStock.get() < quantity) {
            errors.rejectValue("quantity", "Nie posiadasz takiej ilości akcji! Ilość akcji w Twoim portfelu: " + amountOfStock.get());
        }
    }
}