package pl.eizodev.app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
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
    @Enumerated(EnumType.ORDINAL)
    private StockIndex stockIndex;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "average_purchase_price")
    private BigDecimal averagePurchasePrice;

    @Column(name = "percentage_change")
    private String percentageChange;

    @Column(name = "price_change")
    private BigDecimal priceChange;

    @Column(name = "volume")
    private String volume;

    @Column(name = "last_update_date")
    private String lastUpdateDate;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "profit_loss")
    private BigDecimal profitLoss;

    public Stock(StockIndex stockIndex, String ticker, String name, BigDecimal price, String percentageChange, BigDecimal priceChange, String volume, String lastUpdateDate) {
        this.stockIndex = stockIndex;
        this.ticker = ticker;
        this.name = name;
        this.price = price;
        this.priceChange = priceChange;
        this.percentageChange = percentageChange;
        this.volume = volume;
        this.lastUpdateDate = lastUpdateDate;
    }
}