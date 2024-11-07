package com.s1gawron.stockexchange.user.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_wallet")
public class UserWallet {

    private static final BigDecimal DEFAULT_WALLET_BALANCE = new BigDecimal("10000.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id", unique = true, nullable = false)
    private long walletId;

    @Column(name = "owner_id", unique = true, nullable = false)
    private long ownerId;

    @Column(name = "balance_available", nullable = false)
    private BigDecimal balanceAvailable;

    @Column(name = "balance_blocked", nullable = false)
    private BigDecimal balanceBlocked = BigDecimal.ZERO;

    @Column(name = "last_day_value", nullable = false)
    private BigDecimal lastDayValue;

    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;

    protected UserWallet() {
    }

    private UserWallet(final long ownerId, final BigDecimal balanceAvailable, final BigDecimal lastDayValue, final LocalDateTime lastUpdateDate) {
        this.ownerId = ownerId;
        this.balanceAvailable = balanceAvailable;
        this.lastDayValue = lastDayValue;
        this.lastUpdateDate = lastUpdateDate;
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

    public long getWalletId() {
        return walletId;
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

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setWalletId(final Long walletId) {
        this.walletId = walletId;
    }

    public void setLastDayValue(final BigDecimal lastDayWalletValue) {
        this.lastDayValue = lastDayWalletValue;
    }

    public void setLastUpdateDate(final LocalDateTime lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public void updateAfterTransaction(final BigDecimal balanceAfterTransaction, final BigDecimal balanceBlocked) {
        this.balanceAvailable = balanceAfterTransaction;
        this.balanceBlocked = this.balanceBlocked.subtract(balanceBlocked);
    }
}
