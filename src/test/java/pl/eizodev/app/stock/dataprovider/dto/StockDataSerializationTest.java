package pl.eizodev.app.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

class StockDataSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldSerialize() {
        final StockQuoteDTO stockQuoteDTO = new StockQuoteDTO("USD", BigDecimal.valueOf(160.62), BigDecimal.valueOf(1.03), BigDecimal.valueOf(0.6454),
            BigDecimal.valueOf(161), BigDecimal.valueOf(157.63), BigDecimal.valueOf(158.61), BigDecimal.valueOf(159.59));
        final StockDataDTO stockDataDTO = new StockDataDTO("AAPL", "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology", "1980-12-12",
            BigDecimal.valueOf(2530982), 16319.44, stockQuoteDTO, "2022-03-17T21:00:04");
        final String stockDataJsonResult = mapper.writeValueAsString(stockDataDTO);
        final String expectedStockDataJsonResult = Files.readString(Path.of("src/test/resources/stock-data-response.json"));

        final JsonNode expected = mapper.readTree(expectedStockDataJsonResult);
        final JsonNode result = mapper.readTree(stockDataJsonResult);

        Assertions.assertEquals(expected, result);
    }

}