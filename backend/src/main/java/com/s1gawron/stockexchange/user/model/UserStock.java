package com.s1gawron.stockexchange.user.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "public__user_stock", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "wallet_id", "ticker" })
})
public class UserStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "ticker", nullable = false)
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

    public Long getId() {
        return id;
    }

    public Long getWalletId() {
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

    public BigDecimal getAllStockQuantityBD() {
        return BigDecimal.valueOf(quantityAvailable)
            .add(BigDecimal.valueOf(quantityBlocked));
    }

    public void blockStock(final int transactionQuantity) {
        this.quantityAvailable -= transactionQuantity;
        this.quantityBlocked += transactionQuantity;
    }

    public void updateUserStock(final BigDecimal newAveragePurchasePrice, final int newStockQuantity) {
        this.averagePurchasePrice = newAveragePurchasePrice;
        this.quantityAvailable = newStockQuantity;
    }

    public void releaseStock(final int transactionQuantity) {
        if (transactionQuantity > this.quantityBlocked) {
            throw new IllegalStateException("Cannot release more stock than blocked!");
        }

        this.quantityBlocked -= transactionQuantity;
    }

    public static UserStock create(final long walletId, final String ticker, final int quantity, final BigDecimal purchasePrice) {
        return new UserStock(walletId, ticker, quantity, purchasePrice);
    }
}
