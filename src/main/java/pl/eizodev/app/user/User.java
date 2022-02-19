package pl.eizodev.app.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import pl.eizodev.app.stock.Stock;
import pl.eizodev.app.user.dto.UserRegisterDTO;

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
    @NotNull
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

    public static User registerOf(final UserRegisterDTO userRegisterDto) {
        return new User(userRegisterDto.getName(), userRegisterDto.getEmail(), userRegisterDto.getPassword(), userRegisterDto.getBalanceAvailable());
    }
}