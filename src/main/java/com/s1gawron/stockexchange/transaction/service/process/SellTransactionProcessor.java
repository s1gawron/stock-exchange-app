package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SellTransactionProcessor implements TransactionProcessorStrategy {

    private static final Logger log = LoggerFactory.getLogger(SellTransactionProcessor.class);

    private final Transaction transaction;

    private final StockDataProvider stockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public SellTransactionProcessor(final Transaction transaction, final StockDataProvider stockDataProvider, final UserWalletService userWalletService,
        final TransactionDAO transactionDAO) {
        this.transaction = transaction;
        this.stockDataProvider = stockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public boolean canProcessTransaction() {
        if (!transaction.getTransactionType().isSell()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.SELL, transaction.getTransactionType());
        }

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();
        final BigDecimal currentPrice = stockDataProvider.getStockData(transactionPosition.getStockTicker()).stockQuote().currentPrice();

        if (transactionPosition.getStockPriceLimit().compareTo(currentPrice) > 0) {
            log.debug("Could not perform transaction#{} because current stock price: {} is lower than transaction sell price: {}",
                transaction.getTransactionId(), currentPrice, transactionPosition.getStockPriceLimit());

            return false;
        }

        return true;
    }

    @Override
    public void processTransaction() {
        final long walletId = transaction.getWalletId();
        final String stockTicker = transaction.getTransactionPosition().getStockTicker();
        final UserStock userStock = userWalletService.getUserStock(walletId, stockTicker)
            .orElseThrow(() -> StockNotFoundException.createFromTicker(stockTicker));

        if (userHasNoStockAfterTransaction(userStock)) {
            userWalletService.deleteUserStock(userStock);
        } else {
            final int transactionQuantity = transaction.getTransactionPosition().getStockQuantity();
            userStock.releaseStock(transactionQuantity);
            userWalletService.updateUserStock(userStock);
        }

        final BigDecimal currentPrice = stockDataProvider.getStockData(stockTicker).stockQuote().currentPrice();
        final UserWallet userWallet = userWalletService.getUserWallet(transaction.getWalletId())
            .orElseThrow(() -> UserWalletNotFoundException.create(transaction.getWalletId()));
        final BigDecimal balanceAfterTransaction = getBalanceAfterTransaction(currentPrice, userWallet);

        updateUserWalletAfterProcessing(userWallet, balanceAfterTransaction);
        updateTransactionAfterProcessing(balanceAfterTransaction);
    }

    private boolean userHasNoStockAfterTransaction(final UserStock userStock) {
        return userStock.getQuantityAvailable() + userStock.getQuantityBlocked() - transaction.getTransactionPosition().getStockQuantity() == 0;
    }

    private BigDecimal getBalanceAfterTransaction(final BigDecimal currentPrice, final UserWallet userWallet) {
        final BigDecimal stockQuantity = transaction.getTransactionPosition().getStockQuantityBD();
        final BigDecimal transactionCost = currentPrice.multiply(stockQuantity);

        return userWallet.getBalanceAvailable().add(transactionCost);
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
