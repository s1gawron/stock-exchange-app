package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * One trade from the Finnhub websocket.
 *
 * @param timestamp  UNIX milliseconds
 * @param conditions trade condition codes, can be missing
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubTradeDTO(@JsonProperty("s") String symbol,
                              @JsonProperty("p") BigDecimal price,
                              @JsonProperty("t") long timestamp,
                              @JsonProperty("v") BigDecimal volume,
                              @JsonProperty("c") List<String> conditions) {

}
