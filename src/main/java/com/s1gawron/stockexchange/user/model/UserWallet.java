package com.s1gawron.stockexchange.user.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_wallet")
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id", unique = true, nullable = false)
    private long walletId;

    @Column(name = "owner_id", unique = true, nullable = false)
    private long ownerId;

    @Column(name = "balance_available", nullable = false)
    private BigDecimal balanceAvailable;

    @Column(name = "balance_blocked", nullable = false)
    private BigDecimal balanceBlocked;

    @Column(name = "last_day_value", nullable = false)
    private BigDecimal lastDayValue;

    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;

    protected UserWallet() {
    }

    private UserWallet(final long ownerId, final BigDecimal balanceAvailable, final BigDecimal balanceBlocked, final BigDecimal lastDayValue,
        final LocalDateTime lastUpdateDate) {
        this.ownerId = ownerId;
        this.balanceAvailable = balanceAvailable;
        this.balanceBlocked = balanceBlocked;
        this.lastDayValue = lastDayValue;
        this.lastUpdateDate = lastUpdateDate;
    }

    public static UserWallet createNewUserWallet(final long ownerId, final BigDecimal balanceAvailable) {
        return new UserWallet(ownerId, balanceAvailable, BigDecimal.ZERO, balanceAvailable, LocalDateTime.now());
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

}
