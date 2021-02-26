package pl.eizodev.app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import pl.eizodev.app.dto.UserDTO;
import pl.eizodev.app.dto.UserRegisterDTO;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
@DynamicUpdate
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    @NotNull
    private String name;

    @Column(name = "email")
    @NotNull
    private String email;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "role")
    @NotNull
    private String role;

    @Column(name = "enabled")
    @NotNull
    private int active;

    @Column(name = "user_update")
    private LocalDate userUpdate;

    @Column(name = "stock_value")
    private BigDecimal stockValue;

    @Column(name = "balance_available")
    private BigDecimal balanceAvailable;

    @Column(name = "wallet_value")
    private BigDecimal walletValue;

    @Column(name = "prev_wallet_value")
    private BigDecimal prevWalletValue;

    @Column(name = "wallet_percentage_change")
    private BigDecimal walletPercentageChange;

    @OneToMany(mappedBy = "user")
    @Column(name = "user_stock")
    private List<Stock> userStock = new ArrayList<>();

    public User(final String name, final String email, final String password, final BigDecimal balanceAvailable) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.balanceAvailable = balanceAvailable;
    }

    public User(final Long userId, final String name, final BigDecimal stockValue, final BigDecimal balanceAvailable, final BigDecimal walletValue,
                final BigDecimal prevWalletValue, final BigDecimal walletPercentageChange, final List<Stock> userStock) {
        this.userId = userId;
        this.name = name;
        this.stockValue = stockValue;
        this.balanceAvailable = balanceAvailable;
        this.walletValue = walletValue;
        this.prevWalletValue = prevWalletValue;
        this.walletPercentageChange = walletPercentageChange;
        this.userStock = userStock;
    }

    public static User of(final UserDTO userDTO) {
        return new User(userDTO.getUserId(), userDTO.getName(), userDTO.getStockValue(), userDTO.getBalanceAvailable(), userDTO.getWalletValue(),
                userDTO.getPrevWalletValue(), userDTO.getWalletPercentageChange(), Stock.listOf(userDTO.getUserStock()));
    }

    public static User registerOf(final UserRegisterDTO userRegisterDto) {
        return new User(userRegisterDto.getName(), userRegisterDto.getEmail(), userRegisterDto.getPassword(), userRegisterDto.getBalanceAvailable());
    }
}