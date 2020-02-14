package pl.eizodev.app.entity;

import lombok.*;
import pl.eizodev.app.entity.User;

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
    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "iduser")
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
}