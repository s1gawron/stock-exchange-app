package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stockstats.StockFactory;

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
        String index = transaction.getStockIndex();
        int quantity = transaction.getStockQuantity();

        if (transaction.getTransactionType().equals("buy")) {
            StockFactory stockFactory = new StockFactory();
            float price = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), transaction.getStockTicker()).getPrice();
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