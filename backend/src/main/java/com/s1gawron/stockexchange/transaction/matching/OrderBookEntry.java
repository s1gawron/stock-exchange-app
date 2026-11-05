package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * In-memory copy of an open transaction. Only the remaining quantity changes while the transaction is matched.
 */
public final class OrderBookEntry {

    private final long transactionId;

    private final long walletId;

    private final String ticker;

    private final TransactionType transactionType;

    private final BigDecimal priceLimit;

    private final LocalDateTime createDate;

    private BigDecimal remainingQuantity;

    public OrderBookEntry(final long transactionId, final long walletId, final String ticker, final TransactionType transactionType,
        final BigDecimal priceLimit, final BigDecimal remainingQuantity, final LocalDateTime createDate) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.ticker = ticker;
        this.transactionType = transactionType;
        this.priceLimit = priceLimit;
        this.remainingQuantity = remainingQuantity;
        this.createDate = createDate;
    }

    public static OrderBookEntry from(final Transaction transaction) {
        return new OrderBookEntry(transaction.getId(), transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker(),
            transaction.getTransactionType(), transaction.getTransactionPosition().getStockPriceLimit(), transaction.getRemainingQuantity(),
            transaction.getTransactionCreateDate());
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getWalletId() {
        return walletId;
    }

    public String getTicker() {
        return ticker;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getPriceLimit() {
        return priceLimit;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    void decreaseRemainingQuantity(final BigDecimal quantity) {
        this.remainingQuantity = this.remainingQuantity.subtract(quantity);
    }

    boolean isFilled() {
        return remainingQuantity.signum() <= 0;
    }
}
