package com.s1gawron.stockexchange.websocket.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinnhubSubscriptionMessageSerializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldSerializeSubscribe() throws IOException {
        final FinnhubSubscriptionMessage subscribeMessage = FinnhubSubscriptionMessage.subscribe("AAPL");

        final String subscribeMessageJson = mapper.writeValueAsString(subscribeMessage);
        final String expectedSubscribeMessageJson = Files.readString(Path.of("src/test/resources/finnhub-subscribe-message-dto.json"));

        final JsonNode expected = mapper.readTree(expectedSubscribeMessageJson);
        final JsonNode result = mapper.readTree(subscribeMessageJson);

        assertEquals(expected, result);
    }

    @Test
    void shouldSerializeUnsubscribe() throws IOException {
        final FinnhubSubscriptionMessage unsubscribeMessage = FinnhubSubscriptionMessage.unsubscribe("AAPL");

        final String unsubscribeMessageJson = mapper.writeValueAsString(unsubscribeMessage);
        final String expectedUnsubscribeMessageJson = Files.readString(Path.of("src/test/resources/finnhub-unsubscribe-message-dto.json"));

        final JsonNode expected = mapper.readTree(expectedUnsubscribeMessageJson);
        final JsonNode result = mapper.readTree(unsubscribeMessageJson);

        assertEquals(expected, result);
    }

}