package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
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
import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class SellTransactionProcessor implements TransactionProcessorStrategy {

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    private final Clock clock;

    public SellTransactionProcessor(final UserWalletService userWalletService, final TransactionDAO transactionDAO, final Clock clock) {
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
        this.clock = clock;
    }

    @Override
    public boolean canProcessTransaction(final Transaction transaction, final BigDecimal currentPrice) {
        if (!transaction.getTransactionType().isSell()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.SELL, transaction.getTransactionType());
        }

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();

        if (transactionPosition.getStockPriceLimit().compareTo(currentPrice) > 0) {
            throw TransactionProcessingException.createForSell(transactionPosition.getStockPriceLimit(), currentPrice);
        }

        return true;
    }

    @Override
    public void processTransaction(final Transaction transaction, final BigDecimal currentPrice) {
        final long walletId = transaction.getWalletId();
        final String stockTicker = transaction.getTransactionPosition().getStockTicker();
        final UserStock userStock = userWalletService.getUserStock(walletId, stockTicker)
            .orElseThrow(() -> StockNotFoundException.createFromTicker(stockTicker));
        final BigDecimal transactionQuantity = transaction.getTransactionPosition().getStockQuantityBD();

        if (userHasNoStockAfterTransaction(userStock, transactionQuantity.intValue())) {
            userWalletService.deleteUserStock(userStock);
        } else {
            userStock.releaseStock(transactionQuantity.intValue());
            userWalletService.updateUserStock(userStock);
        }

        final UserWallet userWallet = userWalletService.getUserWallet(transaction.getWalletId())
            .orElseThrow(() -> UserWalletNotFoundException.create(transaction.getWalletId()));
        final BigDecimal balanceAfterTransaction = getBalanceAfterTransaction(currentPrice, userWallet, transactionQuantity);

        updateUserWalletAfterProcessing(userWallet, balanceAfterTransaction, transaction.getBalanceBlocked());
        updateTransactionAfterProcessing(transaction, balanceAfterTransaction);
    }

    private boolean userHasNoStockAfterTransaction(final UserStock userStock, final int stockQuantity) {
        return userStock.getQuantityAvailable() + userStock.getQuantityBlocked() - stockQuantity == 0;
    }

    private BigDecimal getBalanceAfterTransaction(final BigDecimal currentPrice, final UserWallet userWallet, final BigDecimal transactionQuantity) {
        final BigDecimal transactionCost = MoneyRounding.I.money(currentPrice.multiply(transactionQuantity));
        return userWallet.getBalanceAvailable().add(transactionCost);
    }

    private void updateUserWalletAfterProcessing(final UserWallet userWallet, final BigDecimal balanceAfterTransaction, final BigDecimal balanceBlocked) {
        userWallet.updateAfterTransaction(balanceAfterTransaction, balanceBlocked);
        userWalletService.updateUserWallet(userWallet);
    }

    private void updateTransactionAfterProcessing(final Transaction transaction, final BigDecimal balanceAfterTransaction) {
        transaction.updateTransactionAfterProcessing(balanceAfterTransaction, LocalDateTime.now(clock));
        transactionDAO.updateTransaction(transaction);
    }
}
