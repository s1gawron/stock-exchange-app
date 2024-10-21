package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionPosition;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

@Component
public class PurchaseTransactionProcessor implements TransactionProcessorStrategy {

    private static final Logger log = LoggerFactory.getLogger(PurchaseTransactionProcessor.class);

    private final FinnhubStockDataProvider finnhubStockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public PurchaseTransactionProcessor(final FinnhubStockDataProvider finnhubStockDataProvider,
        final UserWalletService userWalletService,
        final TransactionDAO transactionDAO) {
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public boolean canProcessTransaction(final Transaction transaction) {
        if (!transaction.getTransactionType().isPurchase()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.PURCHASE, transaction.getTransactionType());
        }

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(transactionPosition.getStockTicker()).stockQuote().currentPrice();

        if (transactionPosition.getStockPriceLimit().compareTo(currentPrice) < 0) {
            log.debug("Could not perform transaction#{} because current stock price: {} is bigger than transaction purchase price: {}",
                transaction.getTransactionId(), currentPrice, transactionPosition.getStockPriceLimit());

            return false;
        }

        return true;
    }

    @Override
    public void processTransaction(final Transaction transaction) {
        final long walletId = transaction.getWalletId();
        final String stockTicker = transaction.getTransactionPosition().getStockTicker();
        final Optional<UserStock> userStock = userWalletService.getUserStock(walletId, stockTicker);
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(stockTicker).stockQuote().currentPrice();
        final int transactionStockQuantity = transaction.getTransactionPosition().getStockQuantity();

        if (userStock.isPresent()) {
            updateUserStock(userStock.get(), transactionStockQuantity, currentPrice);
        } else {
            final UserStock newUserStock = UserStock.create(walletId, stockTicker, transactionStockQuantity, currentPrice);
            userWalletService.saveUserStock(newUserStock);
        }

        final UserWallet userWallet = userWalletService.getUserWallet(walletId)
            .orElseThrow(() -> UserWalletNotFoundException.create(walletId));

        final BigDecimal balanceBlocked = transaction.getBalanceBlocked();
        final BigDecimal balanceAfterTransaction = getBalanceAfterTransaction(transactionStockQuantity, currentPrice, balanceBlocked, userWallet);

        updateUserWalletAfterProcessing(userWallet, balanceAfterTransaction, balanceBlocked);
        updateTransactionAfterProcessing(transaction, balanceAfterTransaction);
    }

    private void updateUserStock(final UserStock userStock, final int transactionStockQuantity, final BigDecimal currentStockPrice) {
        final BigDecimal existingStockValue = userStock.getAveragePurchasePrice().multiply(userStock.getAllStockQuantityBD());
        final BigDecimal stockQuantityBD = BigDecimal.valueOf(transactionStockQuantity);
        final BigDecimal newStockValue = currentStockPrice.multiply(stockQuantityBD);
        final BigDecimal allStockValue = existingStockValue.add(newStockValue);

        final BigDecimal allStockQuantity = userStock.getAllStockQuantityBD().add(stockQuantityBD);
        final BigDecimal newAveragePurchasePrice = allStockValue.divide(allStockQuantity, MathContext.DECIMAL64);
        final int newStockQuantity = userStock.getQuantityAvailable() + transactionStockQuantity;

        userStock.updateUserStock(newAveragePurchasePrice, newStockQuantity);
        userWalletService.updateUserStock(userStock);
    }

    private BigDecimal getBalanceAfterTransaction(final int transactionStockQuantity, final BigDecimal currentPrice, final BigDecimal balanceBlocked,
        final UserWallet userWallet) {
        final BigDecimal stockQuantityBD = BigDecimal.valueOf(transactionStockQuantity);
        final BigDecimal transactionCost = currentPrice.multiply(stockQuantityBD);
        final BigDecimal balanceToRelease = balanceBlocked.subtract(transactionCost);

        return userWallet.getBalanceAvailable().add(balanceToRelease);
    }

    private void updateUserWalletAfterProcessing(final UserWallet userWallet, final BigDecimal balanceAfterTransaction, final BigDecimal balanceBlocked) {
        userWallet.updateAfterTransaction(balanceAfterTransaction, balanceBlocked);
        userWalletService.updateUserWallet(userWallet);
    }

    private void updateTransactionAfterProcessing(final Transaction transaction, final BigDecimal balanceAfterTransaction) {
        transaction.updateTransactionAfterProcessing(balanceAfterTransaction);
        transactionDAO.updateTransaction(transaction);
    }
}
