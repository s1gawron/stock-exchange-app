package com.s1gawron.stockexchange.user.model;

import com.s1gawron.stockexchange.shared.QuantityRounding;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "public__user_stock_position")
public class UserStockPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "quantity_available", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantityAvailable;

    @Column(name = "quantity_blocked", nullable = false, precision = 19, scale = 6)
    private BigDecimal quantityBlocked = QuantityRounding.I.quantity(BigDecimal.ZERO);

    @Column(name = "open_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "open_date", nullable = false)
    private LocalDateTime openDate;

    protected UserStockPosition() {
    }

    public UserStockPosition(final long walletId, final String ticker, final String currency, final BigDecimal quantityAvailable,
        final BigDecimal openPrice, final LocalDateTime openDate) {
        this.walletId = walletId;
        this.ticker = ticker;
        this.currency = currency;
        this.quantityAvailable = QuantityRounding.I.quantity(quantityAvailable);
        this.openPrice = openPrice;
        this.openDate = openDate;
    }

    public static UserStockPosition create(final long walletId, final String ticker, final String currency, final BigDecimal quantity,
        final BigDecimal openPrice, final LocalDateTime openDate) {
        return new UserStockPosition(walletId, ticker, currency, quantity, openPrice, openDate);
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

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getQuantityAvailable() {
        return quantityAvailable;
    }

    public BigDecimal getQuantityBlocked() {
        return quantityBlocked;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public BigDecimal getAllStockQuantity() {
        return quantityAvailable.add(quantityBlocked);
    }

    public boolean isEmpty() {
        return getAllStockQuantity().signum() == 0;
    }

    public void blockStock(final BigDecimal transactionQuantity) {
        if (transactionQuantity.compareTo(this.quantityAvailable) > 0) {
            throw new IllegalStateException("Cannot block more stock than available!");
        }

        this.quantityAvailable = this.quantityAvailable.subtract(transactionQuantity);
        this.quantityBlocked = this.quantityBlocked.add(transactionQuantity);
    }

    public void releaseStock(final BigDecimal transactionQuantity) {
        if (transactionQuantity.compareTo(this.quantityBlocked) > 0) {
            throw new IllegalStateException("Cannot release more stock than blocked!");
        }

        this.quantityBlocked = this.quantityBlocked.subtract(transactionQuantity);
    }
}
