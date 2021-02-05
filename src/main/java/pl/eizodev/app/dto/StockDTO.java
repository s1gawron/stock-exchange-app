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
public class StockDTO {
    private final StockIndex stockIndex;
    private final String ticker;
    private final String name;
    private final BigDecimal price;
    private final String percentageChange;
    private final BigDecimal priceChange;
    private final String volume;
    private final String lastUpdateDate;

    public static StockDTO of(final Stock stock) {
        return StockDTO.builder()
                .stockIndex(stock.getStockIndex())
                .ticker(stock.getTicker())
                .name(stock.getName())
                .price(stock.getPrice())
                .percentageChange(stock.getPercentageChange())
                .priceChange(stock.getPriceChange())
                .volume(stock.getVolume())
                .lastUpdateDate(stock.getLastUpdateDate())
                .build();
    }

    public static List<StockDTO> listOf(final List<Stock> stockList) {
        List<StockDTO> stockDTOList = new ArrayList<>();
        for (Stock stock : stockList) {
            stockDTOList.add(StockDTO.of(stock));
        }
        return stockDTOList;
    }
}
