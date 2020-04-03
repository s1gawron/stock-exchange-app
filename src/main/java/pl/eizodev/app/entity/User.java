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
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;

    @Column(name = "name")
    @NonNull
    private String name;

    @Column(name = "email")
    @NotNull
    private String email;

    @Column(name = "password")
    @NotNull
    private String password;

    @Column(name = "active")
    @NotNull
    private int active;

    @Column(name = "userUpdate")
    private LocalDate userUpdate;

    @Column(name = "stockValue")
    private float stockValue;

    @Column(name = "balanceAvailable")
    private float balanceAvailable;

    @Column(name = "walletValue")
    private float walletValue;

    @Column(name = "prevWalletValue")
    private float prevWalletValue;

    @Column(name = "walletPercChange")
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