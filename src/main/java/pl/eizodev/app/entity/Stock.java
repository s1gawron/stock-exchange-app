package pl.eizodev.app.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_stock")
@DynamicUpdate
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne
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

    public Float parsePrice(String s) {
        return Float.parseFloat(s);
    }

    public String changeString(String sChange) {
        if (sChange.isEmpty()) {
            sChange = "0";
        }
        return sChange;
    }

    public String volumeString(String sVolume) {
        if (sVolume.isEmpty()) {
            sVolume = "0";
        }
        return sVolume;
    }

    public Stock getByTicker(List<Stock> stocks, String ticker) {
        Optional<Stock> first = stocks.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .findFirst();
        return first.get();
    }
}