package pl.eizodev.app;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Stock implements Serializable {
    @Column(name = "TICKER")
    private String ticker;
    @Column(name = "NAME")
    private String name;
    @Column(name = "PRICE")
    private float price;
    @Column(name = "AVERAGE_PRICE")
    private float averagePurchasePrice;
    @Column(name = "CHANGE")
    private String change;
    @Column(name = "VOLUME")
    private String volume;
    @Column(name = "QUANTITY")
    private int quantity;
}