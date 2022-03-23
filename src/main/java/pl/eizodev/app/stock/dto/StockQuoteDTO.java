package pl.eizodev.app.stock.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.eizodev.app.stock.model.StockQuote;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
@Builder
@JsonDeserialize(builder = StockQuoteDTO.StockQuoteDTOBuilder.class)
public class StockQuoteDTO {

    private final String stockCurrency;

    private final BigDecimal currentPrice;

    private final BigDecimal priceChange;

    private final BigDecimal percentageChange;

    private final BigDecimal highestPriceOfTheDay;

    private final BigDecimal lowestPriceOfTheDay;

    private final BigDecimal openPriceOfTheDay;

    private final BigDecimal previousClosePrice;

    public static StockQuoteDTO createFrom(final StockQuote stockQuote) {
        return new StockQuoteDTO(stockQuote.getCurrency(), stockQuote.getCurrentPrice(), stockQuote.getPriceChange(), stockQuote.getPercentageChange(),
            stockQuote.getHighestPriceOfTheDay(), stockQuote.getLowestPriceOfTheDay(), stockQuote.getOpenPriceOfTheDay(), stockQuote.getPreviousClosePrice());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class StockQuoteDTOBuilder {

    }
}
