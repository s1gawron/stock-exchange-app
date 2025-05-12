package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

class StockDataDTOSerializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldSerialize() throws IOException {
        final StockQuoteDTO stockQuoteDTO = new StockQuoteDTO("USD", BigDecimal.valueOf(160.62), BigDecimal.valueOf(1.03), 0.6454,
            BigDecimal.valueOf(161), BigDecimal.valueOf(157.63), BigDecimal.valueOf(158.61), BigDecimal.valueOf(159.59));
        final StockDataDTO stockDataDTO = new StockDataDTO("AAPL", "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology", LocalDate.of(1980, 12, 12),
            BigDecimal.valueOf(2530982), 16319.44, stockQuoteDTO, LocalDateTime.parse("2022-03-17T21:00:04"));
        final String stockDataJsonResult = mapper.writeValueAsString(stockDataDTO);
        final String expectedStockDataJsonResult = Files.readString(Path.of("src/test/resources/stock-data-dto.json"));

        final JsonNode expected = mapper.readTree(expectedStockDataJsonResult);
        final JsonNode result = mapper.readTree(stockDataJsonResult);

        Assertions.assertEquals(expected, result);
    }

}