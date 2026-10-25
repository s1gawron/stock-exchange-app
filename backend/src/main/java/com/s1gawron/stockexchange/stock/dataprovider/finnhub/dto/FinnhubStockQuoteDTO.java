package com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record FinnhubStockQuoteDTO(@JsonProperty("c") BigDecimal currentPrice, @JsonProperty("d") BigDecimal priceChange,
                                   @JsonProperty("dp") double percentageChange, @JsonProperty("h") BigDecimal highestPriceOfTheDay,
                                   @JsonProperty("l") BigDecimal lowestPriceOfTheDay, @JsonProperty("o") BigDecimal openPriceOfTheDay,
                                   @JsonProperty("pc") BigDecimal previousClosePrice, @JsonProperty("t") long lastUpdateDateInEpoch) {

    public boolean isEmpty() {
        return isNullOrZero(this.currentPrice)
            && this.priceChange == null
            && this.percentageChange == 0D
            && isNullOrZero(this.highestPriceOfTheDay)
            && isNullOrZero(this.lowestPriceOfTheDay)
            && isNullOrZero(this.openPriceOfTheDay)
            && isNullOrZero(this.previousClosePrice)
            && this.lastUpdateDateInEpoch == 0;
    }

    private static boolean isNullOrZero(final BigDecimal value) {
        return value == null || value.signum() == 0;
    }
}
