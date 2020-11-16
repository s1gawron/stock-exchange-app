package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.stockstats.StockFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class TransactionValidator implements Validator {

    private final UserRepository userRepository;

    public TransactionValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        Optional<User> userOptional = userRepository.findById(transaction.getUserId());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String index = transaction.getStockIndex();
            int quantity = transaction.getStockQuantity();

            if ("buy".equals(transaction.getTransactionType())) {
                StockFactory stockFactory = new StockFactory();
                BigDecimal price = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), transaction.getStockTicker()).getPrice();
                BigDecimal transactionCost = price.multiply(BigDecimal.valueOf(quantity));
                BigDecimal maxAmount = (user.getBalanceAvailable().divide(transactionCost, RoundingMode.FLOOR));

                if (user.getBalanceAvailable().compareTo(transactionCost) < 0) {
                    errors.rejectValue("stockQuantity", MessageFormat.format("error.notEnoughMoney", maxAmount));
                }
            }

            if ("sell".equals(transaction.getTransactionType())) {
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
}