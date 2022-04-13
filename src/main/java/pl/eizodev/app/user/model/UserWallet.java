package pl.eizodev.app.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.eizodev.app.user.dto.UserWalletDTO;
import pl.eizodev.app.user.dto.UserWalletStockDTO;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_wallet")
@NoArgsConstructor
@Getter
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
    @Column(name = "user_stock")
    private List<UserStock> userStock;

    @Column(name = "wallet_update_date")
    private LocalDateTime lastUpdateDate;

    private UserWallet(final User user, final BigDecimal stockValue, final BigDecimal balanceAvailable, final BigDecimal walletValue,
        final BigDecimal previousWalletValue, final BigDecimal walletPercentageChange, final List<UserStock> userStock, final LocalDateTime lastUpdateDate) {
        this.user = user;
        this.stockValue = stockValue;
        this.balanceAvailable = balanceAvailable;
        this.walletValue = walletValue;
        this.previousWalletValue = previousWalletValue;
        this.walletPercentageChange = walletPercentageChange;
        this.userStock = userStock;
        this.lastUpdateDate = lastUpdateDate;
    }

    public static UserWallet createNewUserWallet(final User user, final BigDecimal balanceAvailable) {
        return new UserWallet(user, BigDecimal.ZERO, balanceAvailable, balanceAvailable, balanceAvailable, BigDecimal.ZERO, List.of(), LocalDateTime.now());
    }

    public void setStockValue(final BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public void setBalanceAvailable(final BigDecimal balanceAvailable) {
        this.balanceAvailable = balanceAvailable;
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

    public void setUserStock(final List<UserStock> userStock) {
        this.userStock = userStock;
    }

    public void setLastUpdateDate(final LocalDateTime updateDate) {
        this.lastUpdateDate = updateDate;
    }

    public UserWalletDTO toUserWalletDTO() {
        final List<UserWalletStockDTO> userWalletStockDTOList = userStock.stream().map(UserStock::toUserWalletStockDTOList).collect(Collectors.toList());
        return new UserWalletDTO(stockValue, balanceAvailable, walletValue, previousWalletValue, walletPercentageChange, userWalletStockDTOList,
            lastUpdateDate);
    }

}
