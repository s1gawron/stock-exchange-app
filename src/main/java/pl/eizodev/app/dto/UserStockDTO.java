package pl.eizodev.app.dto;

import lombok.Builder;
import lombok.Getter;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.StockIndex;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Builder
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
        return UserStockDTO.builder()
                .stockIndex(stock.getStockIndex())
                .ticker(stock.getTicker())
                .name(stock.getName())
                .price(stock.getPrice())
                .averagePurchasePrice(stock.getAveragePurchasePrice())
                .percentageChange(stock.getPercentageChange())
                .priceChange(stock.getPriceChange())
                .volume(stock.getVolume())
                .lastUpdateDate(stock.getLastUpdateDate())
                .quantity(stock.getQuantity())
                .profitLoss(stock.getProfitLoss())
                .build();
    }

    public static List<UserStockDTO> listOf(final List<Stock> stockList) {
        List<UserStockDTO> stockDTOList = new ArrayList<>();
        for (Stock stock : stockList) {
            stockDTOList.add(UserStockDTO.of(stock));
        }
        return stockDTOList;
    }
}
