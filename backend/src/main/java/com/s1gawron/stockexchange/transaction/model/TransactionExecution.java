package com.s1gawron.stockexchange.transaction.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One fill of a transaction.
 */
@Entity
@Table(name = "public__transaction_execution")
public class TransactionExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "execution_date", nullable = false)
    private LocalDateTime executionDate;

    /**
     * Time of the last market trade that was used for this fill.
     */
    @Column(name = "source_trade_date", nullable = false)
    private LocalDateTime sourceTradeDate;

    protected TransactionExecution() {
    }

    public TransactionExecution(final long transactionId, final BigDecimal quantity, final BigDecimal price, final LocalDateTime executionDate,
        final LocalDateTime sourceTradeDate) {
        this.transactionId = transactionId;
        this.quantity = quantity;
        this.price = price;
        this.executionDate = executionDate;
        this.sourceTradeDate = sourceTradeDate;
    }

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public LocalDateTime getSourceTradeDate() {
        return sourceTradeDate;
    }
}
