package pl.eizodev.app.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long stockId;

    @ManyToOne(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.DETACH,
                    CascadeType.MERGE,
                    CascadeType.REFRESH
            })
    @JoinColumn(name = "userId")
    private User user;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private float price;

    @Column(name = "averagePrice")
    private float averagePurchasePrice;

    @Column(name = "change2")
    private String change;

    @Column(name = "volume")
    private String volume;

    @Column(name = "quantity")
    private int quantity;

    public Stock(String ticker, String name, float price, float averagePurchasePrice, String change, String volume, int quantity) {
        this.ticker = ticker;
        this.name = name;
        this.price = price;
        this.averagePurchasePrice = averagePurchasePrice;
        this.change = change;
        this.volume = volume;
        this.quantity = quantity;
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