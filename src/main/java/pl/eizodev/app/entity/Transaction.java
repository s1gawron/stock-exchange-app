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

    @Column(name = "stock_index")
    private String stockIndex;

    @Column(name = "ticker")
    private String stockTicker;

    @Column(name = "price")
    private float stockPrice;

    @Column(name = "quantity")
    private int stockQuantity;

    @Column(name = "user_id")
    private Long userId;

    public Transaction(String transactionType, String stockTicker, float stockPrice, int stockQuantity, Long userId) {
        this.transactionType = transactionType;
        this.stockTicker = stockTicker;
        this.stockPrice = stockPrice;
        this.stockQuantity = stockQuantity;
        this.userId = userId;
    }
}