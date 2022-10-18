package pl.eizodev.app.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.eizodev.app.user.dto.UserWalletStockDTO;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "user_stock")
@Getter
@NoArgsConstructor
public class UserStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_stock_id")
    private long userStockId;

    @ManyToOne
    @JoinColumn(name = "user_wallet_id", nullable = false)
    private UserWallet userWallet;

    //TODO - add relation with transaction after transaction entity refactor

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "average_purchase_price")
    private BigDecimal averagePurchasePrice;

    @Column(name = "quantity")
    private int quantity;

    public UserStock(final String ticker, final BigDecimal averagePurchasePrice, final int quantity) {
        this.ticker = ticker;
        this.averagePurchasePrice = averagePurchasePrice;
        this.quantity = quantity;
    }

    public UserWalletStockDTO toUserWalletStockDTOList() {
        return new UserWalletStockDTO(ticker, averagePurchasePrice, quantity);
    }

    public void setUserWallet(final UserWallet userWallet) {
        this.userWallet = userWallet;
    }
}
