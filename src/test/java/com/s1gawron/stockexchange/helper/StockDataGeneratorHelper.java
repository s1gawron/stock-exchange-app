package com.s1gawron.stockexchange.helper;

import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockQuoteDTO;

import java.math.BigDecimal;

public enum StockDataGeneratorHelper {

    I;

    public StockDataDTO getAppleStock(final BigDecimal currentPrice) {
        final StockQuoteDTO appleStockQuote = new StockQuoteDTO("USD", currentPrice, new BigDecimal("5.00"), new BigDecimal("20.00"),
            new BigDecimal("32.00"), new BigDecimal("25.00"), new BigDecimal("26.00"), new BigDecimal("27.00"));

        return new StockDataDTO("AAPL", "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology", "1980-12-12",
            BigDecimal.valueOf(2530982), 16319.44, appleStockQuote, "2022-03-17T21:00:04");
    }

    public StockDataDTO getAmazonStock(final BigDecimal currentPrice) {
        final StockQuoteDTO amazonStockQuote = new StockQuoteDTO("USD", currentPrice, new BigDecimal("-2.00"), new BigDecimal("7.00"),
            new BigDecimal("32.00"), new BigDecimal("25.00"), new BigDecimal("26.00"), new BigDecimal("27.00"));

        return new StockDataDTO("AMZN", "Amazon Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "E-Commerce", "1980-12-12",
            BigDecimal.valueOf(2530982), 16319.44, amazonStockQuote, "2022-03-17T21:00:04");
    }
}
