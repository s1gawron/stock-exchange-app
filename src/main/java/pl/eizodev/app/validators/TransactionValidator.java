package pl.eizodev.app.validators;

import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import pl.eizodev.app.entities.*;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.stockstats.StockFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class TransactionValidator implements Validator {

    private final UserRepository userRepository;
    private final StockFactory stockFactory;

    @Override
    public boolean supports(final Class<?> aClass) {
        return Transaction.class.equals(aClass);
    }

    @Override
    public void validate(final Object object, final Errors errors) {

        final Transaction transaction = (Transaction) object;

        ValidationUtils.rejectIfEmpty(errors, "stockTicker", "error.stockTicker.empty");
        ValidationUtils.rejectIfEmpty(errors, "transactionType", "error.transactionType.empty");
        ValidationUtils.rejectIfEmpty(errors, "stockQuantity", "error.stockQuantity.empty");

        final User user = userRepository.findByUserId(transaction.getUserId()).get();
        final StockIndex index = transaction.getStockIndex();
        final int quantity = transaction.getStockQuantity();

        if (transaction.getTransactionType() == TransactionType.PURCHASE) {
            final Optional<Stock> stockOptional = stockFactory.getByTicker(index, transaction.getStockTicker());

            if (stockOptional.isPresent()) {
                final BigDecimal price = stockOptional.get().getPrice();
                final BigDecimal transactionCost = price.multiply(BigDecimal.valueOf(quantity));
                final BigDecimal maxAmount = (user.getBalanceAvailable().divide(transactionCost, RoundingMode.FLOOR));

                if (user.getBalanceAvailable().compareTo(transactionCost) < 0) {
                    errors.rejectValue("stockQuantity", MessageFormat.format("error.notEnoughMoney", maxAmount));
                }
            }
        } else {
            final List<Stock> userStock = user.getUserStock();

            final Optional<Integer> amountOfStock = userStock.stream()
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