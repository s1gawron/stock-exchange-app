package com.s1gawron.stockexchange.stock.dataprovider.dto;

import java.math.BigDecimal;

public record StockQuoteDTO(String stockCurrency, BigDecimal currentPrice, BigDecimal priceChange, BigDecimal percentageChange, BigDecimal highestPriceOfTheDay,
                            BigDecimal lowestPriceOfTheDay, BigDecimal openPriceOfTheDay, BigDecimal previousClosePrice) {

    public static StockQuoteDTO createFrom(final String currency, final FinnhubStockQuoteResponseDTO stockQuoteResponse) {
        return new StockQuoteDTO(currency, stockQuoteResponse.currentPrice(), stockQuoteResponse.priceChange(), stockQuoteResponse.percentageChange(),
            stockQuoteResponse.highestPriceOfTheDay(), stockQuoteResponse.lowestPriceOfTheDay(), stockQuoteResponse.openPriceOfTheDay(),
            stockQuoteResponse.previousClosePrice());
    }

}
