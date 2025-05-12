package com.s1gawron.stockexchange.websocket.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public record FinnhubSubscriptionMessage(SubscriptionType type, String symbol) {

    public static FinnhubSubscriptionMessage subscribe(String symbol) {
        return new FinnhubSubscriptionMessage(SubscriptionType.SUBSCRIBE, symbol);
    }

    public static FinnhubSubscriptionMessage unsubscribe(String symbol) {
        return new FinnhubSubscriptionMessage(SubscriptionType.UNSUBSCRIBE, symbol);
    }

    public enum SubscriptionType {
        SUBSCRIBE,
        UNSUBSCRIBE;

        @JsonValue
        public String getValue() {
            return name().toLowerCase();
        }
    }

}
