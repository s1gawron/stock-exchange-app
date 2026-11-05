package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution;
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
import java.util.Optional;

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
    public void processFill(final Transaction transaction, final BigDecimal quantity, final BigDecimal price, final LocalDateTime sourceTradeDate) {
        assertPurchase(transaction);

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();

        if (price.compareTo(transactionPosition.getStockPriceLimit()) > 0) {
            throw new IllegalArgumentException("Fill price: " + price + " is bigger than purchase price limit: " + transactionPosition.getStockPriceLimit()
                + " of transaction#" + transaction.getId());
        }

        final LocalDateTime now = LocalDateTime.now(clock);
        final BigDecimal fillPrice = MoneyRounding.I.money(price);
        final BigDecimal fillCost = MoneyRounding.I.money(fillPrice.multiply(quantity));
        final BigDecimal blockedBalanceToRelease = getBlockedBalanceToRelease(transaction, quantity);

        final UserWallet userWallet = getUserWallet(transaction);
        userWallet.settlePurchaseFill(blockedBalanceToRelease, fillCost);
        userWalletService.updateUserWallet(userWallet);

        addStockToOpenedPosition(transaction, quantity, fillPrice, now);

        transaction.setBalanceBlocked(transaction.getBalanceBlocked().subtract(blockedBalanceToRelease));
        transaction.applyFill(quantity, fillPrice, now);
        transactionDAO.saveTransactionExecution(new TransactionExecution(transaction.getId(), quantity, fillPrice, now, sourceTradeDate));
        transactionDAO.updateTransaction(transaction);
    }

    @Override
    public void cancelTransaction(final Transaction transaction) {
        assertPurchase(transaction);

        final UserWallet userWallet = getUserWallet(transaction);
        userWallet.releaseBalance(transaction.getBalanceBlocked());
        userWalletService.updateUserWallet(userWallet);

        transaction.setBalanceBlocked(BigDecimal.ZERO);
        transaction.cancel(LocalDateTime.now(clock));
        transactionDAO.updateTransaction(transaction);
    }

    /**
     * The money was blocked for the price limit. The last fill releases all money that is still blocked, so rounding leftovers are not kept.
     */
    private BigDecimal getBlockedBalanceToRelease(final Transaction transaction, final BigDecimal quantity) {
        if (quantity.compareTo(transaction.getRemainingQuantity()) == 0) {
            return transaction.getBalanceBlocked();
        }

        final BigDecimal blockedForQuantity = MoneyRounding.I.money(transaction.getTransactionPosition().getStockPriceLimit().multiply(quantity));
        return blockedForQuantity.min(transaction.getBalanceBlocked());
    }

    private void addStockToOpenedPosition(final Transaction transaction, final BigDecimal quantity, final BigDecimal fillPrice, final LocalDateTime now) {
        final Optional<UserStockPosition> openedPosition = transaction.getOpenedPositionId().flatMap(userWalletService::getUserStockPosition);

        if (openedPosition.isPresent()) {
            final UserStockPosition position = openedPosition.get();
            position.addStock(quantity, fillPrice);
            userWalletService.updateUserStockPosition(position);
            return;
        }

        final TransactionPosition transactionPosition = transaction.getTransactionPosition();
        final UserStockPosition position = UserStockPosition.create(transaction.getWalletId(), transactionPosition.getStockTicker(),
            transactionPosition.getStockCurrency(), quantity, fillPrice, now);

        userWalletService.saveUserStockPosition(position);
        transaction.setOpenedPositionId(position.getId());
    }

    private UserWallet getUserWallet(final Transaction transaction) {
        return userWalletService.getUserWallet(transaction.getWalletId())
            .orElseThrow(() -> UserWalletNotFoundException.create(transaction.getWalletId()));
    }

    private void assertPurchase(final Transaction transaction) {
        if (!transaction.getTransactionType().isPurchase()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.PURCHASE, transaction.getTransactionType());
        }
    }
}
