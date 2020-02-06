package pl.eizodev.app;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Stock {
    @Column(name = "ticker")
    private String ticker;
    @Column(name = "name")
    private String name;
    @Column(name = "price")
    private float price;
    @Column(name = "averagePurchasePrice")
    private float averagePurchasePrice;
    @Column(name = "change")
    private String change;
    @Column(name = "volume")
    private String volume;
    @Column(name = "quantity")
    private int quantity;
}
