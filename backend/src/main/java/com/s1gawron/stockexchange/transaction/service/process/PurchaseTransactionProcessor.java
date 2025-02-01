package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionPosition;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PurchaseTransactionProcessor implements TransactionProcessorStrategy {

    private final Transaction transaction;

    private final FinnhubStockDataProvider finnhubStockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public PurchaseTransactionProcessor(final Transaction transaction, final FinnhubStockDataProvider finnhubStockDataProvider,
        final UserWalletService userWalletService,
        final TransactionDAO transactionDAO) {
        this.transaction = transaction;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public boolean canProcessTransaction() {
        if (!transaction.getTransactionType().isPurchase()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.PURCHASE, transaction.getTransactionType());
        }

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(transactionPosition.getStockTicker()).stockQuote().currentPrice();

        if (transactionPosition.getStockPriceLimit().compareTo(currentPrice) < 0) {
            throw TransactionProcessingException.createForPurchase(transactionPosition.getStockPriceLimit(), currentPrice);
        }

        return true;
    }

    @Override
    public void processTransaction() {
        final long walletId = transaction.getWalletId();
        final String stockTicker = transaction.getTransactionPosition().getStockTicker();
        final Optional<UserStock> userStock = userWalletService.getUserStock(walletId, stockTicker);
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(stockTicker).stockQuote().currentPrice();

        if (userStock.isPresent()) {
            updateUserStock(userStock.get(), currentPrice);
        } else {
            final UserStock newUserStock = UserStock.create(walletId, stockTicker, transaction.getTransactionPosition().getStockQuantity(), currentPrice);
            userWalletService.saveUserStock(newUserStock);
        }

        final UserWallet userWallet = userWalletService.getUserWallet(transaction.getWalletId())
            .orElseThrow(() -> UserWalletNotFoundException.create(transaction.getWalletId()));
        final BigDecimal balanceAfterTransaction = getBalanceAfterTransaction(currentPrice, userWallet);

        updateUserWalletAfterProcessing(userWallet, balanceAfterTransaction);
        updateTransactionAfterProcessing(balanceAfterTransaction);
    }

    private void updateUserStock(final UserStock userStock, final BigDecimal currentStockPrice) {
        final BigDecimal existingStockValue = userStock.getAveragePurchasePrice().multiply(userStock.getAllStockQuantityBD());
        final BigDecimal stockQuantityBD = transaction.getTransactionPosition().getStockQuantityBD();
        final BigDecimal newStockValue = currentStockPrice.multiply(stockQuantityBD);
        final BigDecimal allStockValue = existingStockValue.add(newStockValue);

        final BigDecimal allStockQuantity = userStock.getAllStockQuantityBD().add(stockQuantityBD);
        final BigDecimal newAveragePurchasePrice = allStockValue.divide(allStockQuantity, MathContext.DECIMAL64);
        final int newStockQuantity = userStock.getQuantityAvailable() + transaction.getTransactionPosition().getStockQuantity();

        userStock.updateUserStock(newAveragePurchasePrice, newStockQuantity);
        userWalletService.updateUserStock(userStock);
    }

    private BigDecimal getBalanceAfterTransaction(final BigDecimal currentPrice, final UserWallet userWallet) {
        final BigDecimal stockQuantity = transaction.getTransactionPosition().getStockQuantityBD();
        final BigDecimal transactionCost = currentPrice.multiply(stockQuantity);
        final BigDecimal balanceToRelease = transaction.getBalanceBlocked().subtract(transactionCost);

        return userWallet.getBalanceAvailable().add(balanceToRelease);
    }

    private void updateUserWalletAfterProcessing(final UserWallet userWallet, final BigDecimal balanceAfterTransaction) {
        userWallet.updateAfterTransaction(balanceAfterTransaction, transaction.getBalanceBlocked());
        userWalletService.updateUserWallet(userWallet);
    }

    private void updateTransactionAfterProcessing(final BigDecimal balanceAfterTransaction) {
        transaction.updateTransactionAfterProcessing(balanceAfterTransaction);
        transactionDAO.updateTransaction(transaction);
    }
}
