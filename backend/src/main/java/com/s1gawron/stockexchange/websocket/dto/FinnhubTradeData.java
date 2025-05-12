package com.s1gawron.stockexchange.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FinnhubTradeData(List<FinnhubTradeDataDetails> data, String type) {

    public record FinnhubTradeDataDetails(@JsonProperty(value = "p") BigDecimal lastPrice,
                                          @JsonProperty(value = "s") String symbol,
                                          @JsonProperty(value = "t") Instant timestamp,
                                          @JsonProperty(value = "v") BigDecimal volume,
                                          @JsonProperty(value = "c") List<String> tradeConditions) {

    }

    public boolean isTrade() {
        return type.equals("trade");
    }

}
