package pl.eizodev.app.offlineuser;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pl.eizodev.app.dto.TransactionDTO;
import pl.eizodev.app.entities.*;
import pl.eizodev.app.repositories.StockRepository;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.stockstats.StockFactory;
import pl.eizodev.app.validators.TransactionValidator;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@AllArgsConstructor
@Transactional
@Service
public class OfflineStockTransaction {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final StockFactory stockFactory;

    public boolean transactionHasErrors(final TransactionDTO transactionDTO, final BindingResult result) {
        new TransactionValidator(userRepository, stockFactory).validate(Transaction.of(transactionDTO), result);
        return result.hasErrors();
    }

    public void performTransaction(final TransactionDTO transactionDTO) {
        switch (transactionDTO.getTransactionType()) {
            case PURCHASE:
                stockPurchase(transactionDTO);
                break;
            case SELL:
                stockSell(transactionDTO);
                break;
        }
    }

    private void stockPurchase(final TransactionDTO transactionDTO) {
        final Transaction transaction = Transaction.of(transactionDTO);
        final User user = userRepository.findByUserId(transaction.getUserId()).get();
        final String ticker = transaction.getStockTicker();
        final StockIndex index = transaction.getStockIndex();
        final int quantity = transaction.getStockQuantity();
        final Optional<Stock> stockOptional = stockRepository.findByUserAndTicker(user, ticker);
        final Optional<Stock> newStockOptional = stockFactory.getByTicker(index, ticker);

        if (stockOptional.isPresent()) {
            final Stock stock = stockOptional.get();
            final Optional<Stock> stockTempOptional = stockFactory.getByTicker(index, ticker);

            if (stockTempOptional.isPresent()) {
                final BigDecimal priceOfStock = stockTempOptional.get().getPrice();
                final BigDecimal denominator = (stock.getPrice().multiply(BigDecimal.valueOf(stock.getQuantity())))
                        .add(priceOfStock.multiply(BigDecimal.valueOf(quantity)));
                final BigDecimal divider = BigDecimal.valueOf(stock.getQuantity() + quantity);
                final BigDecimal resultOfDivision = denominator.divide(divider, RoundingMode.UNNECESSARY);
                stock.setAveragePurchasePrice(resultOfDivision);
            }
            stock.setQuantity(stock.getQuantity() + quantity);
        } else {
            newStockOptional.ifPresent(newStock -> {
                newStock.setQuantity(quantity);
                newStock.setAveragePurchasePrice(newStock.getPrice());
                newStock.setProfitLoss(BigDecimal.valueOf(0));
                newStock.setUser(user);
                user.getUserStock().add(newStock);
                stockService.saveStock(newStock);
            });
        }
        newStockOptional.ifPresent(newStock -> user.setBalanceAvailable(user.getBalanceAvailable()
                .subtract(newStock.getPrice().multiply(BigDecimal.valueOf(quantity)))));
    }

    private void stockSell(final TransactionDTO transactionDTO) {
        final Transaction transaction = Transaction.of(transactionDTO);
        final User user = userRepository.findByUserId(transaction.getUserId()).get();
        final String ticker = transaction.getStockTicker();
        final StockIndex index = transaction.getStockIndex();
        final int quantity = transaction.getStockQuantity();
        final Optional<Stock> stockOptional = stockRepository.findByUserAndTicker(user, ticker);
        final Optional<Stock> stockTempOptional = stockFactory.getByTicker(index, ticker);

        if (stockTempOptional.isPresent()) {
            final BigDecimal priceOfStock = stockTempOptional.get().getPrice();
            user.setBalanceAvailable(user.getBalanceAvailable().add(priceOfStock.multiply(BigDecimal.valueOf(quantity))));
        }

        if (stockOptional.isPresent()) {
            final Stock stock = stockOptional.get();

            if (stock.getQuantity() == quantity) {
                stockService.deleteStock(stock.getStockId());
            } else {
                stock.setQuantity(stock.getQuantity() - quantity);
            }
        }
    }
}