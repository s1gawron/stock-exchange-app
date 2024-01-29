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

    @Column(name = "wallet_id", nullable = false)
    private long walletId;

    @Column(name = "ticker", unique = true, nullable = false)
    private String ticker;

    @Column(name = "quantity_available", nullable = false)
    private int quantityAvailable;

    @Column(name = "quantity_blocked", nullable = false)
    private int quantityBlocked = 0;

    @Column(name = "average_purchase_price", nullable = false)
    private BigDecimal averagePurchasePrice;

    protected UserStock() {
    }

    public UserStock(final long walletId, final String ticker, final int quantityAvailable, final BigDecimal averagePurchasePrice) {
        this.walletId = walletId;
        this.ticker = ticker;
        this.quantityAvailable = quantityAvailable;
        this.averagePurchasePrice = averagePurchasePrice;
    }

    public void blockStock(final int transactionQuantity) {
        quantityAvailable -= transactionQuantity;
        quantityBlocked += transactionQuantity;
    }

    public long getWalletId() {
        return walletId;
    }

    public String getTicker() {
        return ticker;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public int getQuantityBlocked() {
        return quantityBlocked;
    }

    public BigDecimal getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

}
