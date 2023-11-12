package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.shared.ObjectMapperCreator;
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

    private final ObjectMapper mapper = ObjectMapperCreator.I.getMapper();

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
        final List<FinnhubStockSearchDetailsDTO> resultDetails = result.result();

        assertEquals(expected.count(), result.count());

        expected.result().forEach(expectedSearchDetail -> {
            Assertions.assertAll(
                () -> assertEquals(expectedSearchDetail.description(), resultDetails.get(counter.get()).description()),
                () -> assertEquals(expectedSearchDetail.displaySymbol(), resultDetails.get(counter.get()).displaySymbol()),
                () -> assertEquals(expectedSearchDetail.symbol(), resultDetails.get(counter.get()).symbol()),
                () -> assertEquals(expectedSearchDetail.type(), resultDetails.get(counter.get()).type())
            );
            counter.getAndIncrement();
        });
    }

}