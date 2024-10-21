package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
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

@Component
public class SellTransactionProcessor implements TransactionProcessorStrategy {

    private static final Logger log = LoggerFactory.getLogger(SellTransactionProcessor.class);

    private final FinnhubStockDataProvider finnhubStockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public SellTransactionProcessor(final FinnhubStockDataProvider finnhubStockDataProvider, final UserWalletService userWalletService,
        final TransactionDAO transactionDAO) {
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public boolean canProcessTransaction(final Transaction transaction) {
        if (!transaction.getTransactionType().isSell()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.SELL, transaction.getTransactionType());
        }

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(transactionPosition.getStockTicker()).stockQuote().currentPrice();

        if (transactionPosition.getStockPriceLimit().compareTo(currentPrice) > 0) {
            log.debug("Could not perform transaction#{} because current stock price: {} is lower than transaction sell price: {}",
                transaction.getTransactionId(), currentPrice, transactionPosition.getStockPriceLimit());

            return false;
        }

        return true;
    }

    @Override
    public void processTransaction(final Transaction transaction) {
        final long walletId = transaction.getWalletId();
        final String stockTicker = transaction.getTransactionPosition().getStockTicker();
        final UserStock userStock = userWalletService.getUserStock(walletId, stockTicker)
            .orElseThrow(() -> StockNotFoundException.createFromTicker(stockTicker));
        final int transactionStockQuantity = transaction.getTransactionPosition().getStockQuantity();

        if (userHasNoStockAfterTransaction(userStock, transactionStockQuantity)) {
            userWalletService.deleteUserStock(userStock);
        } else {
            userStock.releaseStock(transactionStockQuantity);
            userWalletService.updateUserStock(userStock);
        }

        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(stockTicker).stockQuote().currentPrice();
        final UserWallet userWallet = userWalletService.getUserWallet(walletId)
            .orElseThrow(() -> UserWalletNotFoundException.create(walletId));
        final BigDecimal balanceAfterTransaction = getBalanceAfterTransaction(transactionStockQuantity, currentPrice, userWallet);

        updateUserWalletAfterProcessing(userWallet, balanceAfterTransaction, transaction.getBalanceBlocked());
        updateTransactionAfterProcessing(transaction, balanceAfterTransaction);
    }

    private boolean userHasNoStockAfterTransaction(final UserStock userStock, final int transactionStockQuantity) {
        return userStock.getQuantityAvailable() + userStock.getQuantityBlocked() - transactionStockQuantity == 0;
    }

    private BigDecimal getBalanceAfterTransaction(final int transactionStockQuantity, final BigDecimal currentPrice, final UserWallet userWallet) {
        final BigDecimal transactionStockQuantityBD = BigDecimal.valueOf(transactionStockQuantity);
        final BigDecimal transactionCost = currentPrice.multiply(transactionStockQuantityBD);
        return userWallet.getBalanceAvailable().add(transactionCost);
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
