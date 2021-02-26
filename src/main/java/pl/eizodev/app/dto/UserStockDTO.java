package pl.eizodev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.StockIndex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class UserStockDTO {
    private final StockIndex stockIndex;
    private final String ticker;
    private final String name;
    private final BigDecimal price;
    private final BigDecimal averagePurchasePrice;
    private final String percentageChange;
    private final BigDecimal priceChange;
    private final String volume;
    private final String lastUpdateDate;
    private final int quantity;
    private final BigDecimal profitLoss;

    private static UserStockDTO of(final Stock stock) {
        return new UserStockDTO(stock.getStockIndex(), stock.getTicker(), stock.getName(), stock.getPrice(), stock.getAveragePurchasePrice(),
                stock.getPercentageChange(), stock.getPriceChange(), stock.getVolume(), stock.getLastUpdateDate(), stock.getQuantity(),
                stock.getProfitLoss());
    }

    public static List<UserStockDTO> listOf(final List<Stock> stockList) {
        final List<UserStockDTO> stockDTOList = new ArrayList<>();

        for (Stock stock : stockList) {
            stockDTOList.add(UserStockDTO.of(stock));
        }

        return stockDTOList;
    }
}
