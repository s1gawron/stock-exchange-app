package pl.eizodev.app;

import lombok.*;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long stockId;
    @Column(name = "ticker")
    private String ticker;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private float price;
    @Column(name = "averagePrice")
    private float averagePurchasePrice;
    @Column(name = "change")
    private String change;
    @Column(name = "volume")
    private String volume;
    @Column(name = "quantity")
    private int quantity;
    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "idUser")
    private User user;
}