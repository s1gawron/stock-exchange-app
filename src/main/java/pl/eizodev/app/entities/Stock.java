package pl.eizodev.app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_stocks")
@DynamicUpdate
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "stock_index")
    private String index;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "average_purchase_price")
    private BigDecimal averagePurchasePrice;

    @Column(name = "percentage_change")
    private String change;

    @Column(name = "volume")
    private String volume;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "profit_loss")
    private BigDecimal profitLoss;

    public Stock(String index, String ticker, String name, BigDecimal price, String change, String volume) {
        this.index = index;
        this.ticker = ticker;
        this.name = name;
        this.price = price;
        this.change = change;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "stockId=" + stockId +
                ", ticker='" + ticker + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", averagePurchasePrice=" + averagePurchasePrice +
                ", change='" + change + '\'' +
                ", volume='" + volume + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}