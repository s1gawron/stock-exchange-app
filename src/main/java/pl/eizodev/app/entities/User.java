package pl.eizodev.app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

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

    @Column(name = "wallet_perc_change")
    private BigDecimal walletPercChange;

    @OneToMany(mappedBy = "user")
    @Column(name = "user_stock")
    private List<Stock> userStock = new ArrayList<>();

    public User(String name, String email, String password, BigDecimal balanceAvailable) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.balanceAvailable = balanceAvailable;
    }
}