package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionPosition;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class PurchaseTransactionProcessor implements TransactionProcessorStrategy {

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    private final Clock clock;

    public PurchaseTransactionProcessor(final UserWalletService userWalletService, final TransactionDAO transactionDAO, final Clock clock) {
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
        this.clock = clock;
    }

    @Override
    public boolean canProcessTransaction(final Transaction transaction, final BigDecimal currentPrice) {
        if (!transaction.getTransactionType().isPurchase()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.PURCHASE, transaction.getTransactionType());
        }

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();

        if (transactionPosition.getStockPriceLimit().compareTo(currentPrice) < 0) {
            throw TransactionProcessingException.createForPurchase(transactionPosition.getStockPriceLimit(), currentPrice);
        }

        return true;
    }

    @Override
    public void processTransaction(final Transaction transaction, final BigDecimal currentPrice) {
        final TransactionPosition transactionPosition = transaction.getTransactionPosition();
        final BigDecimal stockQuantity = transactionPosition.getStockQuantity();
        final UserStockPosition userStockPosition = UserStockPosition.create(transaction.getWalletId(), transactionPosition.getStockTicker(),
            transactionPosition.getStockCurrency(), stockQuantity, MoneyRounding.I.money(currentPrice), LocalDateTime.now(clock));

        userWalletService.saveUserStockPosition(userStockPosition);

        final UserWallet userWallet = userWalletService.getUserWallet(transaction.getWalletId())
            .orElseThrow(() -> UserWalletNotFoundException.create(transaction.getWalletId()));
        final BigDecimal balanceAfterTransaction = getBalanceAfterTransaction(currentPrice, stockQuantity, transaction.getBalanceBlocked(), userWallet);

        updateUserWalletAfterProcessing(userWallet, balanceAfterTransaction, transaction.getBalanceBlocked());
        updateTransactionAfterProcessing(transaction, balanceAfterTransaction);
    }

    private BigDecimal getBalanceAfterTransaction(final BigDecimal currentPrice, final BigDecimal stockQuantity, final BigDecimal balanceBlocked,
        final UserWallet userWallet) {
        final BigDecimal transactionCost = MoneyRounding.I.money(currentPrice.multiply(stockQuantity));
        final BigDecimal balanceToRelease = balanceBlocked.subtract(transactionCost);

        return userWallet.getBalanceAvailable().add(balanceToRelease);
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
