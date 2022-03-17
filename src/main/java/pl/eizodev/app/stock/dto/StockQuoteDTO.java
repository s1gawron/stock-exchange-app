package pl.eizodev.app.stock.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.stock.model.StockQuote;

import java.math.BigDecimal;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
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
}
