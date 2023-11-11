package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

    public static StockQuoteDTO createFrom(final String currency, final FinnhubStockQuoteResponseDTO stockQuoteResponse) {
        return new StockQuoteDTO(currency, stockQuoteResponse.getCurrentPrice(), stockQuoteResponse.getPriceChange(), stockQuoteResponse.getPercentageChange(),
            stockQuoteResponse.getHighestPriceOfTheDay(), stockQuoteResponse.getLowestPriceOfTheDay(), stockQuoteResponse.getOpenPriceOfTheDay(),
            stockQuoteResponse.getPreviousClosePrice());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class StockQuoteDTOBuilder {

    }
}
