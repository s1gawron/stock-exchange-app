package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import pl.eizodev.app.entity.Stock;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stocks.StockWIG20;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class TransactionValidator implements Validator {

    final UserService userService;

    public TransactionValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Transaction.class.equals(aClass);
    }

    @Override
    public void validate(Object object, Errors errors) {

        Transaction transaction = (Transaction) object;

        ValidationUtils.rejectIfEmpty(errors, "stockTicker", "error.stockTicker.empty");
        ValidationUtils.rejectIfEmpty(errors, "transactionType", "error.transactionType.empty");
        ValidationUtils.rejectIfEmpty(errors, "stockQuantity", "error.stockQuantity.empty");

        Optional<User> userOptional = userService.findById(transaction.getUserId());
        User user = userOptional.get();
        int quantity = transaction.getStockQuantity();

        if (transaction.getTransactionType().equals("buy")) {
            StockWIG20 stockWIG20 = new StockWIG20();
            float price = stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), transaction.getStockTicker()).getPrice();
            float transactionCost = quantity * price;
            int maxAmount = (int) Math.floor((user.getBalanceAvailable() / transactionCost));

            if (user.getBalanceAvailable() < transactionCost) {
                errors.rejectValue("stockQuantity", MessageFormat.format("error.notEnoughMoney", maxAmount));
            }
        }

        if (transaction.getTransactionType().equals("sell")) {
            List<Stock> userStock = user.getUserStock();

            Optional<Integer> amountOfStock = userStock.stream()
                    .filter(o -> o.getTicker().equals(transaction.getStockTicker()))
                    .map(Stock::getQuantity)
                    .findFirst();

            if (!amountOfStock.isPresent()) {
                errors.rejectValue("stockQuantity", "error.noSuchStock");
            } else if (amountOfStock.get() < quantity) {
                errors.rejectValue("stockQuantity", MessageFormat.format("error.notEnoughStock", amountOfStock.get()));
            }
        }
    }
}