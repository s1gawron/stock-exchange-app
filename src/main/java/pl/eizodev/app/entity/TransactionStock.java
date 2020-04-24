package pl.eizodev.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transaction_stock")
@DynamicUpdate
public class TransactionStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_stock_id")
    private Long transactionStockId;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "price")
    private float price;

    @Column(name = "quantity")
    private int quantity;

    @JsonIgnore
    @OneToOne(mappedBy = "transactionStock")
    private Transaction transaction;

    public TransactionStock(String ticker, float price, int quantity) {
        this.ticker = ticker;
        this.price = price;
        this.quantity = quantity;
    }
}