package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.matching.Fill;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.TransactionProcessorStrategy;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionFillService {

    private static final Logger log = LoggerFactory.getLogger(TransactionFillService.class);

    private final PurchaseTransactionProcessor purchaseTransactionProcessor;

    private final SellTransactionProcessor sellTransactionProcessor;

    private final TransactionDAO transactionDAO;

    private final UserWalletService userWalletService;

    private final Clock clock;

    public TransactionFillService(final PurchaseTransactionProcessor purchaseTransactionProcessor, final SellTransactionProcessor sellTransactionProcessor,
        final TransactionDAO transactionDAO, final UserWalletService userWalletService, final Clock clock) {
        this.purchaseTransactionProcessor = purchaseTransactionProcessor;
        this.sellTransactionProcessor = sellTransactionProcessor;
        this.transactionDAO = transactionDAO;
        this.userWalletService = userWalletService;
        this.clock = clock;
    }

    /**
     * Saves the fills of one wallet. The wallet is locked first, so a fill and a cancel of the same transaction cannot run at the same time. A fill
     * of a transaction that is not open anymore (e.g. canceled after the matching) is skipped.
     *
     * @return ids of the transactions whose fill was skipped or changed, so their order book entries must be read again from the database
     */
    @Transactional
    public List<Long> applyFills(final long walletId, final List<Fill> fills) {
        userWalletService.lockUserWallet(walletId);

        final List<Long> transactionIdsToReload = new ArrayList<>();

        for (final Fill fill : fills) {
            if (fill.walletId() != walletId) {
                throw new IllegalArgumentException("Fill of transaction#" + fill.transactionId() + " does not belong to wallet#" + walletId);
            }

            final Optional<Transaction> transaction = transactionDAO.getTransactionById(fill.transactionId());

            if (transaction.isEmpty() || !transaction.get().isOpen()) {
                log.info("Fill of transaction#{} skipped, because the transaction is not open anymore", fill.transactionId());
                transactionIdsToReload.add(fill.transactionId());
                continue;
            }

            final BigDecimal remainingQuantity = transaction.get().getRemainingQuantity();
            final BigDecimal quantity = fill.quantity().min(remainingQuantity);

            if (quantity.compareTo(fill.quantity()) != 0) {
                log.warn("Fill quantity: {} of transaction#{} is bigger than remaining quantity: {}", fill.quantity(), fill.transactionId(),
                    remainingQuantity);
                transactionIdsToReload.add(fill.transactionId());
            }

            if (quantity.signum() > 0) {
                getStrategy(transaction.get()).processFill(transaction.get(), quantity, fill.price(),
                    LocalDateTime.ofInstant(fill.lastTradeTimestamp(), clock.getZone()));
            }
        }

        return transactionIdsToReload;
    }

    private TransactionProcessorStrategy getStrategy(final Transaction transaction) {
        return transaction.getTransactionType().isPurchase() ? purchaseTransactionProcessor : sellTransactionProcessor;
    }
}
