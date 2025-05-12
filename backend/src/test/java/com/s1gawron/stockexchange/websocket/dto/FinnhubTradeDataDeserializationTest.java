package com.s1gawron.stockexchange.websocket.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinnhubTradeDataDeserializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldDeserialize() throws IOException {
        final String tradeDataJson = Files.readString(Path.of("src/test/resources/finnhub-trade-data.json"));
        final FinnhubTradeData result = mapper.readValue(tradeDataJson, FinnhubTradeData.class);
        final List<FinnhubTradeData.FinnhubTradeDataDetails> details = result.data();

        assertTrue(result.isTrade());
        assertEquals(1, details.size());
        assertEquals(new BigDecimal("7296.89"), details.get(0).lastPrice());
        assertEquals("BINANCE:BTCUSDT", details.get(0).symbol());
        assertEquals(Instant.ofEpochSecond(1575526691134L), details.get(0).timestamp());
        assertEquals(new BigDecimal("0.011467"), details.get(0).volume());
    }

}