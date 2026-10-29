package com.s1gawron.stockexchange.transaction.model;

import com.s1gawron.stockexchange.shared.QuantityRounding;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class TransactionPosition {

    @Column(name = "stock_ticker", nullable = false)
    private String stockTicker;

    @Column(name = "stock_price_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal stockPriceLimit;

    @Column(name = "stock_quantity", nullable = false, precision = 19, scale = 6)
    private BigDecimal stockQuantity;

    @Column(name = "stock_currency", nullable = false, length = 3)
    private String stockCurrency;

    protected TransactionPosition() {
    }

    public TransactionPosition(final String stockTicker, final BigDecimal stockPriceLimit, final BigDecimal stockQuantity, final String stockCurrency) {
        this.stockTicker = stockTicker;
        this.stockPriceLimit = stockPriceLimit;
        this.stockQuantity = QuantityRounding.I.quantity(stockQuantity);
        this.stockCurrency = stockCurrency;
    }

    public static TransactionPosition createFrom(final TransactionRequestDTO transactionRequestDTO, final String stockCurrency) {
        return new TransactionPosition(transactionRequestDTO.stockTicker(), transactionRequestDTO.price(), transactionRequestDTO.quantity(), stockCurrency);
    }

    public String getStockTicker() {
        return stockTicker;
    }

    public BigDecimal getStockPriceLimit() {
        return stockPriceLimit;
    }

    public BigDecimal getStockQuantity() {
        return stockQuantity;
    }

    public String getStockCurrency() {
        return stockCurrency;
    }
}
