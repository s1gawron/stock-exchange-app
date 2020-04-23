package pl.eizodev.app.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "transactions")
@DynamicUpdate
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction_type")
    private String transactionType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "transaction_stock_id")
    private TransactionStock transactionStock;

    public Transaction(String transactionType, User user, TransactionStock transactionStock) {
        this.transactionType = transactionType;
        this.user = user;
        this.transactionStock = transactionStock;
    }
}