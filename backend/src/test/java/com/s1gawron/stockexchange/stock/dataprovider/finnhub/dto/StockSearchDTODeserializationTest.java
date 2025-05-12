package com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockSearchDTODeserializationTest {

    private static final StockSearchDTO FINNHUB_STOCK_SEARCH_RESPONSE = new StockSearchDTO(4, List.of(
        new StockSearchDetailsDTO("APPLE INC", "AAPL", "AAPL", "Common Stock"),
        new StockSearchDetailsDTO("APPLE INC", "AAPL.SW", "AAPL.SW", "Common Stock"),
        new StockSearchDetailsDTO("APPLE INC", "APC.BE", "APC.BE", "Common Stock"),
        new StockSearchDetailsDTO("APPLE INC", "APC.DE", "APC.DE", "Common Stock")
    ));

    private static final StockSearchDTO FINNHUB_STOCK_SEARCH_STOCK_NOT_FOUND_RESPONSE = new StockSearchDTO(0, List.of());

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldDeserialize() throws IOException {
        final String stockSearchJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-dto.json"));
        final StockSearchDTO result = mapper.readValue(stockSearchJsonResponse, StockSearchDTO.class);

        assertStockSearchResponse(FINNHUB_STOCK_SEARCH_RESPONSE, result);
    }

    @Test
    void shouldDeserializeWhenStockIsNotFound() throws IOException {
        final String stockSearchJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-stock-not-found-dto.json"));
        final StockSearchDTO result = mapper.readValue(stockSearchJsonResponse, StockSearchDTO.class);

        assertStockSearchResponse(FINNHUB_STOCK_SEARCH_STOCK_NOT_FOUND_RESPONSE, result);
    }

    private void assertStockSearchResponse(final StockSearchDTO expected, final StockSearchDTO result) {
        final AtomicInteger counter = new AtomicInteger(0);
        final List<StockSearchDetailsDTO> resultDetails = result.result();

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