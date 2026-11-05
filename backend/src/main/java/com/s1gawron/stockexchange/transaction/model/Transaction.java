package com.s1gawron.stockexchange.transaction.model;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.shared.QuantityRounding;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotOpenException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "public__transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "transaction_create_date", nullable = false)
    private LocalDateTime transactionCreateDate;

    /**
     * Date when the transaction reached a final status (FILLED or CANCELED).
     */
    @Column(name = "transaction_process_date")
    private LocalDateTime transactionProcessDate;

    /**
     * Money that is still blocked in the wallet for this transaction. Used only by purchase transactions.
     */
    @Column(name = "balance_blocked", precision = 19, scale = 4)
    private BigDecimal balanceBlocked;

    @Embedded
    private TransactionPosition transactionPosition;

    @Column(name = "filled_quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal filledQuantity = QuantityRounding.I.quantity(BigDecimal.ZERO);

    @Column(name = "average_fill_price", precision = 19, scale = 4)
    private BigDecimal averageFillPrice;

    /**
     * Position from which the user asked to sell. Null when the sell takes the oldest positions first, and for purchases.
     */
    @Column(name = "sell_position_id")
    private Long sellPositionId;

    /**
     * Position opened by the fills of a purchase transaction. Null until the first fill, and for sells.
     */
    @Column(name = "opened_position_id")
    private Long openedPositionId;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Transaction() {
    }

    public Transaction(final long walletId, final TransactionType transactionType, final TransactionStatus transactionStatus,
        final LocalDateTime transactionCreateDate, final TransactionPosition transactionPosition) {
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.transactionCreateDate = transactionCreateDate;
        this.transactionPosition = transactionPosition;
    }

    public static Transaction createFrom(final long walletId, final TransactionRequestDTO transactionRequestDTO, final String stockCurrency,
        final LocalDateTime transactionCreateDate) {
        final TransactionPosition transactionPosition = TransactionPosition.createFrom(transactionRequestDTO, stockCurrency);
        final Transaction transaction = new Transaction(walletId, transactionRequestDTO.type(), TransactionStatus.NEW, transactionCreateDate,
            transactionPosition);
        transaction.sellPositionId = transactionRequestDTO.type().isSell() ? transactionRequestDTO.positionId() : null;

        return transaction;
    }

    public Long getId() {
        return id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public TransactionPosition getTransactionPosition() {
        return transactionPosition;
    }

    public LocalDateTime getTransactionCreateDate() {
        return transactionCreateDate;
    }

    public LocalDateTime getTransactionProcessDate() {
        return transactionProcessDate;
    }

    public BigDecimal getBalanceBlocked() {
        return Optional.ofNullable(balanceBlocked).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getFilledQuantity() {
        return filledQuantity;
    }

    public BigDecimal getRemainingQuantity() {
        return transactionPosition.getStockQuantity().subtract(filledQuantity);
    }

    public Optional<BigDecimal> getAverageFillPrice() {
        return Optional.ofNullable(averageFillPrice);
    }

    public Optional<Long> getSellPositionId() {
        return Optional.ofNullable(sellPositionId);
    }

    public Optional<Long> getOpenedPositionId() {
        return Optional.ofNullable(openedPositionId);
    }

    public boolean isOpen() {
        return transactionStatus.isOpen();
    }

    public void setBalanceBlocked(final BigDecimal balanceBlocked) {
        this.balanceBlocked = balanceBlocked;
    }

    public void setOpenedPositionId(final Long openedPositionId) {
        this.openedPositionId = openedPositionId;
    }

    /**
     * Adds one fill to the transaction: updates the filled quantity, the average fill price and the status.
     */
    public void applyFill(final BigDecimal quantity, final BigDecimal price, final LocalDateTime fillDate) {
        assertOpen();

        if (quantity.signum() <= 0 || quantity.compareTo(getRemainingQuantity()) > 0) {
            throw new IllegalArgumentException("Fill quantity: " + quantity + " must be positive and not bigger than remaining quantity: "
                + getRemainingQuantity() + " of transaction#" + id);
        }

        final BigDecimal previousValue = getAverageFillPrice().orElse(BigDecimal.ZERO).multiply(filledQuantity);
        final BigDecimal newFilledQuantity = filledQuantity.add(quantity);

        this.averageFillPrice = previousValue.add(price.multiply(quantity)).divide(newFilledQuantity, MoneyRounding.MONEY_SCALE, RoundingMode.HALF_UP);
        this.filledQuantity = QuantityRounding.I.quantity(newFilledQuantity);

        if (getRemainingQuantity().signum() == 0) {
            this.transactionStatus = TransactionStatus.FILLED;
            this.transactionProcessDate = fillDate;
        } else {
            this.transactionStatus = TransactionStatus.PARTIALLY_FILLED;
        }
    }

    public void cancel(final LocalDateTime cancelDate) {
        assertOpen();

        this.transactionStatus = TransactionStatus.CANCELED;
        this.transactionProcessDate = cancelDate;
    }

    private void assertOpen() {
        if (!isOpen()) {
            throw TransactionNotOpenException.create(id, transactionStatus);
        }
    }
}
