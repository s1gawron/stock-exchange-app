package com.s1gawron.stockexchange.transaction.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Quantity of one stock position that is blocked by an open sell transaction. Fills and cancels release the stock from these positions.
 */
@Entity
@Table(name = "public__transaction_stock_block")
public class TransactionStockBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "position_id", nullable = false)
    private Long positionId;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    protected TransactionStockBlock() {
    }

    public TransactionStockBlock(final long transactionId, final long positionId, final BigDecimal quantity) {
        this.transactionId = transactionId;
        this.positionId = positionId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void decreaseQuantity(final BigDecimal quantityToRemove) {
        if (quantityToRemove.compareTo(quantity) > 0) {
            throw new IllegalStateException("Cannot remove more stock than blocked!");
        }

        this.quantity = this.quantity.subtract(quantityToRemove);
    }

    public boolean isEmpty() {
        return quantity.signum() == 0;
    }
}
