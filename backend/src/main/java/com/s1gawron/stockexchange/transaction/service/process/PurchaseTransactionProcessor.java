package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

@Component
public class PurchaseTransactionProcessor implements TransactionProcessorStrategy {

    private final StockDataProvider finnhubStockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public PurchaseTransactionProcessor(final StockDataProvider finnhubStockDataProvider, final UserWalletService userWalletService,
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
            throw TransactionProcessingException.createForPurchase(transactionPosition.getStockPriceLimit(), currentPrice);
        }

        return true;
    }

    @Override
    public void processTransaction(final Transaction transaction) {
        final long walletId = transaction.getWalletId();
        final String stockTicker = transaction.getTransactionPosition().getStockTicker();
        final Optional<UserStock> userStock = userWalletService.getUserStock(walletId, stockTicker);
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(stockTicker).stockQuote().currentPrice();
        final BigDecimal stockQuantity = transaction.getTransactionPosition().getStockQuantityBD();

        if (userStock.isPresent()) {
            updateUserStock(userStock.get(), stockQuantity, currentPrice);
        } else {
            final UserStock newUserStock = UserStock.create(walletId, stockTicker, stockQuantity.intValue(), currentPrice);
            userWalletService.saveUserStock(newUserStock);
        }

        final UserWallet userWallet = userWalletService.getUserWallet(transaction.getWalletId())
            .orElseThrow(() -> UserWalletNotFoundException.create(transaction.getWalletId()));
        final BigDecimal balanceAfterTransaction = getBalanceAfterTransaction(currentPrice, transaction.getTransactionPosition().getStockQuantityBD(),
            transaction.getBalanceBlocked(), userWallet);

        updateUserWalletAfterProcessing(userWallet, balanceAfterTransaction, transaction.getBalanceBlocked());
        updateTransactionAfterProcessing(transaction, balanceAfterTransaction);
    }

    private void updateUserStock(final UserStock userStock, final BigDecimal stockQuantity, final BigDecimal currentStockPrice) {
        final BigDecimal existingStockValue = userStock.getAveragePurchasePrice().multiply(userStock.getAllStockQuantityBD());
        final BigDecimal newStockValue = currentStockPrice.multiply(stockQuantity);
        final BigDecimal allStockValue = existingStockValue.add(newStockValue);

        final BigDecimal allStockQuantity = userStock.getAllStockQuantityBD().add(stockQuantity);
        final BigDecimal newAveragePurchasePrice = allStockValue.divide(allStockQuantity, MathContext.DECIMAL64);
        final int newStockQuantity = userStock.getQuantityAvailable() + stockQuantity.intValue();

        userStock.updateUserStock(newAveragePurchasePrice, newStockQuantity);
        userWalletService.updateUserStock(userStock);
    }

    private BigDecimal getBalanceAfterTransaction(final BigDecimal currentPrice, final BigDecimal stockQuantity, final BigDecimal balanceBlocked,
        final UserWallet userWallet) {
        final BigDecimal transactionCost = currentPrice.multiply(stockQuantity);
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
