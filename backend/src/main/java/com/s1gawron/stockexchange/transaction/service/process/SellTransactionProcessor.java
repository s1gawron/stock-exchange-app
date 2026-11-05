package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution;
import com.s1gawron.stockexchange.transaction.model.TransactionStockBlock;
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
    public void processFill(final Transaction transaction, final BigDecimal quantity, final BigDecimal price, final LocalDateTime sourceTradeDate) {
        assertSell(transaction);

        if (price.compareTo(transaction.getTransactionPosition().getStockPriceLimit()) < 0) {
            throw new IllegalArgumentException("Fill price: " + price + " is lower than sell price limit: "
                + transaction.getTransactionPosition().getStockPriceLimit() + " of transaction#" + transaction.getId());
        }

        final LocalDateTime now = LocalDateTime.now(clock);
        final BigDecimal fillPrice = MoneyRounding.I.money(price);

        releaseSoldStock(transaction, quantity);

        final UserWallet userWallet = getUserWallet(transaction);
        userWallet.addBalance(MoneyRounding.I.money(fillPrice.multiply(quantity)));
        userWalletService.updateUserWallet(userWallet);

        transaction.applyFill(quantity, fillPrice, now);
        transactionDAO.saveTransactionExecution(new TransactionExecution(transaction.getId(), quantity, fillPrice, now, sourceTradeDate));
        transactionDAO.updateTransaction(transaction);
    }

    @Override
    public void cancelTransaction(final Transaction transaction) {
        assertSell(transaction);

        for (final TransactionStockBlock block : transactionDAO.getTransactionStockBlocks(transaction.getId())) {
            final UserStockPosition position = getPosition(block);
            position.unblockStock(block.getQuantity());
            userWalletService.updateUserStockPosition(position);
            transactionDAO.deleteTransactionStockBlock(block);
        }

        transaction.cancel(LocalDateTime.now(clock));
        transactionDAO.updateTransaction(transaction);
    }

    /**
     * Removes the sold quantity from the positions blocked by this transaction, in the order in which they were blocked.
     */
    private void releaseSoldStock(final Transaction transaction, final BigDecimal quantity) {
        BigDecimal quantityToRelease = quantity;

        for (final TransactionStockBlock block : transactionDAO.getTransactionStockBlocks(transaction.getId())) {
            if (quantityToRelease.signum() == 0) {
                break;
            }

            final BigDecimal blockQuantityToRelease = quantityToRelease.min(block.getQuantity());
            final UserStockPosition position = getPosition(block);

            position.releaseStock(blockQuantityToRelease);
            block.decreaseQuantity(blockQuantityToRelease);
            quantityToRelease = quantityToRelease.subtract(blockQuantityToRelease);

            if (block.isEmpty()) {
                transactionDAO.deleteTransactionStockBlock(block);
            } else {
                transactionDAO.updateTransactionStockBlock(block);
            }

            if (position.isEmpty()) {
                userWalletService.deleteUserStockPosition(position);
            } else {
                userWalletService.updateUserStockPosition(position);
            }
        }

        if (quantityToRelease.signum() > 0) {
            throw new IllegalStateException("Transaction#" + transaction.getId() + " does not have enough blocked stock for fill quantity: " + quantity);
        }
    }

    private UserStockPosition getPosition(final TransactionStockBlock block) {
        return userWalletService.getUserStockPosition(block.getPositionId())
            .orElseThrow(() -> new IllegalStateException("Position#" + block.getPositionId() + " blocked by transaction#" + block.getTransactionId()
                + " does not exist!"));
    }

    private UserWallet getUserWallet(final Transaction transaction) {
        return userWalletService.getUserWallet(transaction.getWalletId())
            .orElseThrow(() -> UserWalletNotFoundException.create(transaction.getWalletId()));
    }

    private void assertSell(final Transaction transaction) {
        if (!transaction.getTransactionType().isSell()) {
            throw WrongTransactionTypeForProcessingException.create(TransactionType.SELL, transaction.getTransactionType());
        }
    }
}
