package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinnhubStockSearchResponseDeserializationTest {

    private static final FinnhubStockSearchResponseDTO FINNHUB_STOCK_SEARCH_RESPONSE = new FinnhubStockSearchResponseDTO(4, List.of(
        new FinnhubStockSearchDetailsDTO("APPLE INC", "AAPL", "AAPL", "Common Stock"),
        new FinnhubStockSearchDetailsDTO("APPLE INC", "AAPL.SW", "AAPL.SW", "Common Stock"),
        new FinnhubStockSearchDetailsDTO("APPLE INC", "APC.BE", "APC.BE", "Common Stock"),
        new FinnhubStockSearchDetailsDTO("APPLE INC", "APC.DE", "APC.DE", "Common Stock")
    ));

    private static final FinnhubStockSearchResponseDTO FINNHUB_STOCK_SEARCH_STOCK_NOT_FOUND_RESPONSE = new FinnhubStockSearchResponseDTO(0, List.of());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        final String stockSearchJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-response.json"));
        final FinnhubStockSearchResponseDTO result = mapper.readValue(stockSearchJsonResponse, FinnhubStockSearchResponseDTO.class);

        assertStockSearchResponse(FINNHUB_STOCK_SEARCH_RESPONSE, result);
    }

    @Test
    @SneakyThrows
    void shouldDeserializeWhenStockIsNotFound() {
        final String stockSearchJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-stock-not-found-response.json"));
        final FinnhubStockSearchResponseDTO result = mapper.readValue(stockSearchJsonResponse, FinnhubStockSearchResponseDTO.class);

        assertStockSearchResponse(FINNHUB_STOCK_SEARCH_STOCK_NOT_FOUND_RESPONSE, result);
    }

    private void assertStockSearchResponse(final FinnhubStockSearchResponseDTO expected, final FinnhubStockSearchResponseDTO result) {
        final AtomicInteger counter = new AtomicInteger(0);
        final List<FinnhubStockSearchDetailsDTO> resultDetails = result.getResult();

        assertEquals(expected.getCount(), result.getCount());

        expected.getResult().forEach(expectedSearchDetail -> {
            Assertions.assertAll(
                () -> assertEquals(expectedSearchDetail.getDescription(), resultDetails.get(counter.get()).getDescription()),
                () -> assertEquals(expectedSearchDetail.getDisplaySymbol(), resultDetails.get(counter.get()).getDisplaySymbol()),
                () -> assertEquals(expectedSearchDetail.getSymbol(), resultDetails.get(counter.get()).getSymbol()),
                () -> assertEquals(expectedSearchDetail.getType(), resultDetails.get(counter.get()).getType())
            );
            counter.getAndIncrement();
        });
    }

}