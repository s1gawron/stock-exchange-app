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
        return new StockDTO(stock.getStockIndex(), stock.getTicker(), stock.getName(), stock.getPrice(), stock.getPercentageChange(),
                stock.getPriceChange(), stock.getVolume(), stock.getLastUpdateDate());
    }

    public static List<StockDTO> listOf(final List<Stock> stockList) {
        final List<StockDTO> stockDTOList = new ArrayList<>();

        for (Stock stock : stockList) {
            stockDTOList.add(StockDTO.of(stock));
        }

        return stockDTOList;
    }
}
