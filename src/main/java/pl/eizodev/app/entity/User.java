package pl.eizodev.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
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
    private float stockValue;

    @Column(name = "balance_available")
    private float balanceAvailable;

    @Column(name = "wallet_value")
    private float walletValue;

    @Column(name = "prev_wallet_value")
    private float prevWalletValue;

    @Column(name = "wallet_perc_change")
    private float walletPercChange;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.REFRESH
            })
    private List<Stock> userStock = new ArrayList<>();

    public User(String name, String email, String password, float balanceAvailable) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.balanceAvailable = balanceAvailable;
    }
}