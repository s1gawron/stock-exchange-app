package pl.eizodev.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity(name = "User")
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;
    @Column(name = "name")
    private String name;
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
    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.REFRESH
            },
            orphanRemoval = true)
    private List<Stock> userStock = new ArrayList<>();

    public User(String name, LocalDate userUpdate, float stockValue, float balanceAvailable, float walletValue, float prevWalletValue, List<Stock> userStock) {
        this.name = name;
        this.userUpdate = userUpdate;
        this.stockValue = stockValue;
        this.balanceAvailable = balanceAvailable;
        this.walletValue = walletValue;
        this.prevWalletValue = prevWalletValue;
        this.userStock = userStock;
    }

    public void addStockToList(Stock stock) {
        userStock.add(stock);
    }
}