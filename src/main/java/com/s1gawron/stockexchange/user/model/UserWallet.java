package com.s1gawron.stockexchange.user.model;

import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletStockDTO;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_wallet")
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_wallet_id")
    private Long walletId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "stock_value")
    private BigDecimal stockValue;

    @Column(name = "balance_available")
    private BigDecimal balanceAvailable;

    @Column(name = "wallet_value")
    private BigDecimal walletValue;

    @Column(name = "previous_wallet_value")
    private BigDecimal previousWalletValue;

    @Column(name = "wallet_percentage_change")
    private BigDecimal walletPercentageChange;

    @OneToMany(mappedBy = "userWallet")
    @Column(name = "user_stocks")
    private final List<UserStock> userStocks = new ArrayList<>();

    @Column(name = "wallet_update_date")
    private LocalDateTime lastUpdateDate;

    protected UserWallet() {
    }

    private UserWallet(final User user, final BigDecimal stockValue, final BigDecimal balanceAvailable, final BigDecimal walletValue,
        final BigDecimal previousWalletValue, final BigDecimal walletPercentageChange, final LocalDateTime lastUpdateDate) {
        this.user = user;
        this.stockValue = stockValue;
        this.balanceAvailable = balanceAvailable;
        this.walletValue = walletValue;
        this.previousWalletValue = previousWalletValue;
        this.walletPercentageChange = walletPercentageChange;
        this.lastUpdateDate = lastUpdateDate;
    }

    public static UserWallet createNewUserWallet(final User user, final BigDecimal balanceAvailable) {
        return new UserWallet(user, BigDecimal.ZERO, balanceAvailable, balanceAvailable, balanceAvailable, BigDecimal.ZERO, LocalDateTime.now());
    }

    public void setStockValue(final BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public void setWalletValue(final BigDecimal walletValue) {
        this.walletValue = walletValue;
    }

    public void setPreviousWalletValue(final BigDecimal previousWalletValue) {
        this.previousWalletValue = previousWalletValue;
    }

    public void setWalletPercentageChange(final BigDecimal walletPercentageChange) {
        this.walletPercentageChange = walletPercentageChange;
    }

    public void addUserStocks(final List<UserStock> userStocks) {
        userStocks.forEach(userStock -> {
            this.userStocks.add(userStock);
            userStock.setUserWallet(this);
        });
    }

    public void setLastUpdateDate(final LocalDateTime updateDate) {
        this.lastUpdateDate = updateDate;
    }

    public UserWalletDTO toUserWalletDTO() {
        final List<UserWalletStockDTO> userWalletStockDTOList = userStocks.stream().map(UserStock::toUserWalletStockDTOList).collect(Collectors.toList());
        return new UserWalletDTO(stockValue, balanceAvailable, walletValue, previousWalletValue, walletPercentageChange, userWalletStockDTOList,
            lastUpdateDate);
    }

    public Long getWalletId() {
        return walletId;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getStockValue() {
        return stockValue;
    }

    public BigDecimal getBalanceAvailable() {
        return balanceAvailable;
    }

    public BigDecimal getWalletValue() {
        return walletValue;
    }

    public BigDecimal getPreviousWalletValue() {
        return previousWalletValue;
    }

    public BigDecimal getWalletPercentageChange() {
        return walletPercentageChange;
    }

    public List<UserStock> getUserStocks() {
        return userStocks;
    }

    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
}
