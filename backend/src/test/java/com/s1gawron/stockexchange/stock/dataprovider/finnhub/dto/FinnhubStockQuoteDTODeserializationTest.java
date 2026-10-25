package com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto;

import tools.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinnhubStockQuoteDTODeserializationTest {

    private static final FinnhubStockQuoteDTO STOCK_QUOTE_RESPONSE = new FinnhubStockQuoteDTO(BigDecimal.valueOf(159.59),
        BigDecimal.valueOf(4.5), 2.9015, BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05),
        BigDecimal.valueOf(155.09), 1647460804);

    private static final FinnhubStockQuoteDTO STOCK_QUOTE_STOCK_NOT_FOUND_RESPONSE = new FinnhubStockQuoteDTO(BigDecimal.valueOf(0),
        null, 0D, BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(0), 0);

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldDeserialize() throws IOException {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-dto.json"));
        final FinnhubStockQuoteDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubStockQuoteDTO.class);

        assertStockQuoteResponse(STOCK_QUOTE_RESPONSE, result);
    }

    @Test
    void shouldDeserializeWhenStockIsNotFound() throws IOException {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-stock-not-found-dto.json"));
        final FinnhubStockQuoteDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubStockQuoteDTO.class);

        assertStockQuoteResponse(STOCK_QUOTE_STOCK_NOT_FOUND_RESPONSE, result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldBeEmptyWhenZeroValuesHaveScale() {
        final FinnhubStockQuoteDTO stockQuote = new FinnhubStockQuoteDTO(new BigDecimal("0.00"), null, 0D, new BigDecimal("0.0"), BigDecimal.ZERO,
            BigDecimal.ZERO, new BigDecimal("0.00"), 0);

        assertTrue(stockQuote.isEmpty());
    }

    @Test
    void shouldNotBeEmptyWhenOnlyCurrentPriceIsZero() {
        final FinnhubStockQuoteDTO stockQuote = new FinnhubStockQuoteDTO(BigDecimal.ZERO, null, 0D, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.valueOf(155.09), 1647460804);

        assertFalse(stockQuote.isEmpty());
        assertFalse(STOCK_QUOTE_RESPONSE.isEmpty());
    }

    private void assertStockQuoteResponse(final FinnhubStockQuoteDTO expected, final FinnhubStockQuoteDTO result) {
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