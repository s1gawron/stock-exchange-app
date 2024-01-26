package com.s1gawron.stockexchange.user.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "user_stock")
public class UserStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id", unique = true, nullable = false)
    private long stockId;

    @Column(name = "user_wallet_id", nullable = false)
    private long userWalletId;

    @Column(name = "ticker", unique = true, nullable = false)
    private String ticker;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "average_purchase_price", nullable = false)
    private BigDecimal averagePurchasePrice;

    protected UserStock() {
    }

    public UserStock(final long userWalletId, final String ticker, final int quantity, final BigDecimal averagePurchasePrice) {
        this.userWalletId = userWalletId;
        this.ticker = ticker;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
    }

    public String getTicker() {
        return ticker;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAveragePurchasePrice() {
        return averagePurchasePrice;
    }
}
