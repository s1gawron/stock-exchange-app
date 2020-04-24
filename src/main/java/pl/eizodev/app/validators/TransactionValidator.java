package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.TransactionStock;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.stocks.StockWIG20;

import java.util.List;
import java.util.Optional;

public class TransactionValidator {

    public void hasEnoughMoney(Transaction transaction, Errors errors) {
        User user = transaction.getUser();
        TransactionStock transactionStock = transaction.getTransactionStock();
        float transactionCost = transactionStock.getQuantity() * transactionStock.getPrice();
        StockWIG20 stockWIG20 = new StockWIG20();
        int maxAmount = (int) Math.floor((user.getBalanceAvailable() / (stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), transactionStock.getTicker()).getPrice())));

        if (user.getBalanceAvailable() < transactionCost) {
            errors.rejectValue("quantity", "Nie masz odpowiednich srodkow, maksymalna ilosc akcji jakie mozesz kupic: " + maxAmount);
        }
    }

    public void hasEnoughStock(Transaction transaction, Errors errors) {
        User user = transaction.getUser();
        List<Stock> userStock = user.getUserStock();
        String ticker = transaction.getTransactionStock().getTicker();
        int quantity = transaction.getTransactionStock().getQuantity();

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