package com.s1gawron.stockexchange.transaction.model;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class TransactionPosition {

    @Column(name = "stock_ticker", nullable = false)
    private String stockTicker;

    @Column(name = "stock_purchase_price", nullable = false)
    private BigDecimal stockPurchasePrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    protected TransactionPosition() {
    }

    public TransactionPosition(final String stockTicker, final BigDecimal stockPurchasePrice, final int stockQuantity) {
        this.stockTicker = stockTicker;
        this.stockPurchasePrice = stockPurchasePrice;
        this.stockQuantity = stockQuantity;
    }

    public static TransactionPosition createFrom(final TransactionRequestDTO transactionRequestDTO) {
        return new TransactionPosition(transactionRequestDTO.stockTicker(), transactionRequestDTO.stockPrice(), transactionRequestDTO.stockQuantity());
    }
}
