package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubStockQuoteDTO;

import java.math.BigDecimal;

public record StockQuoteDTO(String stockCurrency, BigDecimal currentPrice, BigDecimal priceChange, double percentagePriceChange,
                            BigDecimal highestPriceOfTheDay, BigDecimal lowestPriceOfTheDay, BigDecimal openPriceOfTheDay, BigDecimal previousClosePrice) {

    public static StockQuoteDTO createFrom(final String currency, final FinnhubStockQuoteDTO stockQuoteResponse) {
        return new StockQuoteDTO(currency, stockQuoteResponse.currentPrice(), stockQuoteResponse.priceChange(), stockQuoteResponse.percentageChange(),
            stockQuoteResponse.highestPriceOfTheDay(), stockQuoteResponse.lowestPriceOfTheDay(), stockQuoteResponse.openPriceOfTheDay(),
            stockQuoteResponse.previousClosePrice());
    }

}
