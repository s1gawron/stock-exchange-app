package pl.eizodev.app.stock.dataprovider.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-response.json"));
        final FinnhubStockQuoteResponseDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubStockQuoteResponseDTO.class);

        assertStockQuoteResponse(STOCK_QUOTE_RESPONSE, result);
    }

    @Test
    @SneakyThrows
    void shouldDeserializeWhenStockIsNotFound() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-stock-not-found-response.json"));
        final FinnhubStockQuoteResponseDTO result = mapper.readValue(companyProfileJsonResponse, FinnhubStockQuoteResponseDTO.class);

        assertStockQuoteResponse(STOCK_QUOTE_STOCK_NOT_FOUND_RESPONSE, result);
    }

    private void assertStockQuoteResponse(final FinnhubStockQuoteResponseDTO expected, final FinnhubStockQuoteResponseDTO result) {
        Assertions.assertAll(
            () -> assertEquals(expected.getCurrentPrice(), result.getCurrentPrice()),
            () -> assertEquals(expected.getPriceChange(), result.getPriceChange()),
            () -> assertEquals(expected.getPercentageChange(), result.getPercentageChange()),
            () -> assertEquals(expected.getHighestPriceOfTheDay(), result.getHighestPriceOfTheDay()),
            () -> assertEquals(expected.getLowestPriceOfTheDay(), result.getLowestPriceOfTheDay()),
            () -> assertEquals(expected.getPreviousClosePrice(), result.getPreviousClosePrice())
        );
    }

}