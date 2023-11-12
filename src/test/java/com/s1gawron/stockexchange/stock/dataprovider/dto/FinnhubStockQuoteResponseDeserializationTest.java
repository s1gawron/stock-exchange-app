package com.s1gawron.stockexchange.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.shared.ObjectMapperCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinnhubStockQuoteResponseDeserializationTest {

    private static final FinnhubStockQuoteResponseDTO STOCK_QUOTE_RESPONSE = new FinnhubStockQuoteResponseDTO(BigDecimal.valueOf(159.59),
        BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015), BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05),
        BigDecimal.valueOf(155.09), 1647460804);

    private static final FinnhubStockQuoteResponseDTO STOCK_QUOTE_STOCK_NOT_FOUND_RESPONSE = new FinnhubStockQuoteResponseDTO(BigDecimal.valueOf(0),
        null, null, BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), 0);

    private final ObjectMapper mapper = ObjectMapperCreator.I.getMapper();

    @Test
    void shouldDeserialize() throws IOException {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-response.json"));
        final FinnhubStockQuoteResponseDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubStockQuoteResponseDTO.class);

        assertStockQuoteResponse(STOCK_QUOTE_RESPONSE, result);
    }

    @Test
    void shouldDeserializeWhenStockIsNotFound() throws IOException {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-stock-not-found-response.json"));
        final FinnhubStockQuoteResponseDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubStockQuoteResponseDTO.class);

        assertStockQuoteResponse(STOCK_QUOTE_STOCK_NOT_FOUND_RESPONSE, result);
    }

    private void assertStockQuoteResponse(final FinnhubStockQuoteResponseDTO expected, final FinnhubStockQuoteResponseDTO result) {
        Assertions.assertAll(
            () -> assertEquals(expected.currentPrice(), result.currentPrice()),
            () -> assertEquals(expected.priceChange(), result.priceChange()),
            () -> assertEquals(expected.percentageChange(), result.percentageChange()),
            () -> assertEquals(expected.highestPriceOfTheDay(), result.highestPriceOfTheDay()),
            () -> assertEquals(expected.lowestPriceOfTheDay(), result.lowestPriceOfTheDay()),
            () -> assertEquals(expected.previousClosePrice(), result.previousClosePrice())
        );
    }

}