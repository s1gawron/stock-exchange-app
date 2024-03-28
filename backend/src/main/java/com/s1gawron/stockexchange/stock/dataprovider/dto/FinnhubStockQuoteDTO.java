package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FinnhubStockQuoteDTO(@JsonProperty("c") BigDecimal currentPrice, @JsonProperty("d") BigDecimal priceChange,
                                   @JsonProperty("dp") double percentageChange, @JsonProperty("h") BigDecimal highestPriceOfTheDay,
                                   @JsonProperty("l") BigDecimal lowestPriceOfTheDay, @JsonProperty("o") BigDecimal openPriceOfTheDay,
                                   @JsonProperty("pc") BigDecimal previousClosePrice, @JsonProperty("t") long lastUpdateDateInEpoch) {

    public boolean isEmpty() {
        return this.currentPrice.equals(new BigDecimal(0))
            && this.priceChange == null
            && this.percentageChange == 0D
            && this.highestPriceOfTheDay.equals(new BigDecimal(0))
            && this.lowestPriceOfTheDay.equals(new BigDecimal(0))
            && this.openPriceOfTheDay.equals(new BigDecimal(0))
            && this.previousClosePrice.equals(new BigDecimal(0))
            && this.lastUpdateDateInEpoch == 0;
    }
}
