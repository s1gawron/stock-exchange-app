package pl.eizodev.app.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.REFRESH
            })
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private float price;

    @Column(name = "average_purchase_price")
    private float averagePurchasePrice;

    @Column(name = "change2")
    private String change;

    @Column(name = "volume")
    private String volume;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "profit_loss")
    private float profitLoss;

    public Stock(String ticker, String name, float price, String change, String volume) {
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

    public Stock getByTicker(List<Stock> stocks, String ticker) {
        Optional<Stock> first = stocks.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .findFirst();
        return first.get();
    }
}