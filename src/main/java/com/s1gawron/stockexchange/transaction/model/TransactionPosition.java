package com.s1gawron.stockexchange.transaction.model;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class TransactionPosition {

    @Column(name = "stock_ticker", nullable = false)
    private String stockTicker;

    @Column(name = "stock_price_limit", nullable = false)
    private BigDecimal stockPriceLimit;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    protected TransactionPosition() {
    }

    public TransactionPosition(final String stockTicker, final BigDecimal stockPriceLimit, final int stockQuantity) {
        this.stockTicker = stockTicker;
        this.stockPriceLimit = stockPriceLimit;
        this.stockQuantity = stockQuantity;
    }

    public static TransactionPosition createFrom(final TransactionRequestDTO transactionRequestDTO) {
        return new TransactionPosition(transactionRequestDTO.stockTicker(), transactionRequestDTO.price(), transactionRequestDTO.quantity());
    }

    public String getStockTicker() {
        return stockTicker;
    }

    public BigDecimal getStockPriceLimit() {
        return stockPriceLimit;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public BigDecimal getStockQuantityBD() {
        return BigDecimal.valueOf(stockQuantity);
    }
}
