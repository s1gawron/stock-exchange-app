package pl.eizodev.app;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    private String ticker;
    private String name;
    private float price;
    private float averagePurchasePrice;
    private String change;
    private String volume;
    private int quantity;
}
