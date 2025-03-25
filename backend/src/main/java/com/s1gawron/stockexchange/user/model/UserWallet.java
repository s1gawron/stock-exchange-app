package com.s1gawron.stockexchange.user.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "public__user_wallet")
public class UserWallet {

    private static final BigDecimal DEFAULT_WALLET_BALANCE = new BigDecimal("10000.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "balance_available", nullable = false)
    private BigDecimal balanceAvailable;

    @Column(name = "balance_blocked", nullable = false)
    private BigDecimal balanceBlocked = BigDecimal.ZERO;

    @Column(name = "last_day_value", nullable = false)
    private BigDecimal lastDayValue;

    @Column(name = "last_day_update_date", nullable = false)
    private LocalDateTime lastDayUpdateDate;

    protected UserWallet() {
    }

    private UserWallet(final long userId, final BigDecimal balanceAvailable, final BigDecimal lastDayValue, final LocalDateTime lastDayUpdateDate) {
        this.userId = userId;
        this.balanceAvailable = balanceAvailable;
        this.lastDayValue = lastDayValue;
        this.lastDayUpdateDate = lastDayUpdateDate;
    }

    public static UserWallet createNewUserWallet(final long ownerId) {
        return new UserWallet(ownerId, DEFAULT_WALLET_BALANCE, DEFAULT_WALLET_BALANCE, LocalDateTime.now());
    }

    public static UserWallet createUserWallet(final long ownerId, final BigDecimal balanceAvailable) {
        return new UserWallet(ownerId, balanceAvailable, balanceAvailable, LocalDateTime.now());
    }

    public void blockBalance(final BigDecimal transactionCost) {
        this.balanceAvailable = this.balanceAvailable.subtract(transactionCost);
        this.balanceBlocked = this.balanceBlocked.add(transactionCost);
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getBalanceAvailable() {
        return balanceAvailable;
    }

    public BigDecimal getBalanceBlocked() {
        return balanceBlocked;
    }

    public BigDecimal getLastDayValue() {
        return lastDayValue;
    }

    public LocalDateTime getLastDayUpdateDate() {
        return lastDayUpdateDate;
    }

    public void setLastDayValue(final BigDecimal lastDayWalletValue) {
        this.lastDayValue = lastDayWalletValue;
    }

    public void setLastDayUpdateDate(final LocalDateTime lastUpdateDate) {
        this.lastDayUpdateDate = lastUpdateDate;
    }

    public void updateAfterTransaction(final BigDecimal balanceAfterTransaction, final BigDecimal balanceBlocked) {
        this.balanceAvailable = balanceAfterTransaction;
        this.balanceBlocked = this.balanceBlocked.subtract(balanceBlocked);
    }
}
