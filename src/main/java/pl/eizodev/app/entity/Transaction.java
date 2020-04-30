package pl.eizodev.app.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "ticker")
    private String stockTicker;

    @Column(name = "price")
    private float stockPrice;

    @Column(name = "quantity")
    private int stockQuantity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Transaction(String transactionType, String stockTicker, float stockPrice, int stockQuantity, User user) {
        this.transactionType = transactionType;
        this.stockTicker = stockTicker;
        this.stockPrice = stockPrice;
        this.stockQuantity = stockQuantity;
        this.user = user;
    }
}