package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Message from the Finnhub websocket. The type is {@code trade}, {@code ping} or {@code error}.
 *
 * @param data trades, only for type {@code trade}
 * @param msg  error message, only for type {@code error}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnhubWebSocketMessageDTO(String type, List<FinnhubTradeDTO> data, String msg) {

    public static final String TRADE_TYPE = "trade";

    public static final String PING_TYPE = "ping";

    public static final String ERROR_TYPE = "error";

}
