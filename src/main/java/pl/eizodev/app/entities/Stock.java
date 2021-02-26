package pl.eizodev.app.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import pl.eizodev.app.dto.UserStockDTO;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public Stock(final StockIndex stockIndex, final String ticker, final String name, final BigDecimal price, final String percentageChange,
                 final BigDecimal priceChange, final String volume, final String lastUpdateDate) {
        this.stockIndex = stockIndex;
        this.ticker = ticker;
        this.name = name;
        this.price = price;
        this.priceChange = priceChange;
        this.percentageChange = percentageChange;
        this.volume = volume;
        this.lastUpdateDate = lastUpdateDate;
    }

    private static Stock of(final UserStockDTO userStockDTO) {
        return new Stock(userStockDTO.getStockIndex(), userStockDTO.getTicker(), userStockDTO.getName(), userStockDTO.getPrice(),
                userStockDTO.getPercentageChange(), userStockDTO.getPriceChange(), userStockDTO.getVolume(), userStockDTO.getLastUpdateDate());
    }

    public static List<Stock> listOf(final List<UserStockDTO> stockDTOList) {
        final List<Stock> stockList = new ArrayList<>();

        for (UserStockDTO userStockDTO : stockDTOList) {
            stockList.add(Stock.of(userStockDTO));
        }

        return stockList;
    }
}