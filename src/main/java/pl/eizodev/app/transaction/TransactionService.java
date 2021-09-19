package pl.eizodev.app.transaction;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.eizodev.app.stock.*;
import pl.eizodev.app.transaction.dto.TransactionDTO;
import pl.eizodev.app.transaction.dto.TransactionResultDTO;
import pl.eizodev.app.transaction.exception.NoStockInUserWalletException;
import pl.eizodev.app.transaction.exception.NotEnoughMoneyException;
import pl.eizodev.app.transaction.exception.NotEnoughStockException;
import pl.eizodev.app.user.User;
import pl.eizodev.app.user.UserRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class TransactionService {

    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final StockFactory stockFactory;

    public TransactionResultDTO performTransaction(final TransactionDTO transactionDTO) {
        if (transactionDTO.getTransactionType() == TransactionType.PURCHASE) {
            return stockPurchase(transactionDTO);
        } else {
            return stockSell(transactionDTO);
        }
    }

    private TransactionResultDTO stockPurchase(final TransactionDTO transactionDTO) {
        final Transaction transaction = Transaction.of(transactionDTO);
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User user = userRepository.findByName(authentication.getName()).get();
        final String ticker = transaction.getStockTicker();
        final StockIndex index = transaction.getStockIndex();
        final int quantity = transaction.getStockQuantity();
        final Optional<Stock> userStockOptional = stockRepository.findByUserAndTicker(user, ticker);
        final Stock stock = stockFactory.getByTicker(index, ticker);
        final BigDecimal price = stock.getPrice();
        final BigDecimal transactionCost = price.multiply(BigDecimal.valueOf(quantity));
        final BigDecimal maxAmountOfStockToPurchase = (user.getBalanceAvailable().divide(transactionCost, RoundingMode.FLOOR));

        if (user.getBalanceAvailable().compareTo(transactionCost) < 0) {
            throw NotEnoughMoneyException.create(transactionCost, user.getBalanceAvailable(), String.valueOf(maxAmountOfStockToPurchase), stock.getName());
        }

        if (userStockOptional.isPresent()) {
            final Stock userStock = userStockOptional.get();
            final Stock stockTemp = stockFactory.getByTicker(index, ticker);
            final BigDecimal priceOfStock = stockTemp.getPrice();
            final BigDecimal denominator = (userStock.getPrice().multiply(BigDecimal.valueOf(userStock.getQuantity())))
                    .add(priceOfStock.multiply(BigDecimal.valueOf(quantity)));
            final BigDecimal divider = BigDecimal.valueOf(userStock.getQuantity() + quantity);
            final BigDecimal resultOfDivision = denominator.divide(divider, RoundingMode.UNNECESSARY);

            userStock.setAveragePurchasePrice(resultOfDivision);
            userStock.setQuantity(userStock.getQuantity() + quantity);
        } else {
            stock.setQuantity(quantity);
            stock.setAveragePurchasePrice(stock.getPrice());
            stock.setProfitLoss(BigDecimal.valueOf(0));
            stock.setUser(user);
            user.getUserStock().add(stock);
            stockService.saveStock(stock);
        }
        user.setBalanceAvailable(user.getBalanceAvailable().subtract(stock.getPrice().multiply(BigDecimal.valueOf(quantity))));

        return new TransactionResultDTO(user.getUserId(), user.getName(), TransactionType.PURCHASE, stock.getName(), quantity, transactionCost, user.getBalanceAvailable());
    }

    private TransactionResultDTO stockSell(final TransactionDTO transactionDTO) {
        final Transaction transaction = Transaction.of(transactionDTO);
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User user = userRepository.findByName(authentication.getName()).get();
        final String ticker = transaction.getStockTicker();
        final int transactionStockQuantity = transaction.getStockQuantity();
        final Optional<Stock> userStockOptional = stockRepository.findByUserAndTicker(user, ticker);
        final Stock stock = stockFactory.getByTicker(transaction.getStockIndex(), ticker);
        BigDecimal transactionCost;

        if (userStockOptional.isPresent()) {
            final Stock userStock = userStockOptional.get();

            if (userStock.getQuantity() < transactionStockQuantity) {
                throw NotEnoughStockException.create(userStock.getName(), userStock.getQuantity(), transactionStockQuantity);
            }

            final BigDecimal priceOfStock = stock.getPrice();
            transactionCost = priceOfStock.multiply(BigDecimal.valueOf(transactionStockQuantity));
            user.setBalanceAvailable(user.getBalanceAvailable().add(transactionCost));

            if (userStock.getQuantity() == transactionStockQuantity) {
                stockService.deleteStock(userStock.getStockId());
            } else {
                userStock.setQuantity(userStock.getQuantity() - transactionStockQuantity);
            }
        } else {
            throw NoStockInUserWalletException.create(stock.getName());
        }

        return new TransactionResultDTO(user.getUserId(), user.getName(), TransactionType.SELL, stock.getName(), transactionStockQuantity, transactionCost, user.getBalanceAvailable());
    }
}