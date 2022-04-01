package pl.eizodev.app.stock.dataprovider;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import pl.eizodev.app.stock.dataprovider.dto.*;
import pl.eizodev.app.stock.dataprovider.exception.FinnhubConnectionFailedException;
import pl.eizodev.app.stock.dataprovider.exception.StockNotFoundException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class StockDataProviderTest {

    private static final String STOCK_TICKER = "AAPL";

    private static final FinnhubStockSearchResponseDTO FINNHUB_STOCK_SEARCH_RESPONSE = new FinnhubStockSearchResponseDTO(4, List.of(
        new FinnhubStockSearchDetailsDTO("APPLE INC", "AAPL", "AAPL", "Common Stock"),
        new FinnhubStockSearchDetailsDTO("APPLE INC", "AAPL.SW", "AAPL.SW", "Common Stock"),
        new FinnhubStockSearchDetailsDTO("APPLE INC", "APC.BE", "APC.BE", "Common Stock"),
        new FinnhubStockSearchDetailsDTO("APPLE INC", "APC.DE", "APC.DE", "Common Stock")
    ));

    private static final FinnhubCompanyProfileResponseDTO COMPANY_PROFILE_RESPONSE = new FinnhubCompanyProfileResponseDTO(STOCK_TICKER, "Apple Inc", "US",
        "NASDAQ NMS - GLOBAL MARKET", "Technology", "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, "USD", "https://finnhub.io/api/logo?symbol=AAPL",
        "14089961010.0", "https://www.apple.com/");

    private static final FinnhubStockQuoteResponseDTO STOCK_QUOTE_RESPONSE = new FinnhubStockQuoteResponseDTO(BigDecimal.valueOf(159.59),
        BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015), BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05),
        BigDecimal.valueOf(155.09), 1647460804);

    private MockWebServer mockWebServer;

    private StockDataProvider stockDataProvider;

    @BeforeEach
    void setupMockWebServer() {
        mockWebServer = new MockWebServer();
        stockDataProvider = new StockDataProvider("test", mockWebServer.url("/api/v1").url().toString());
    }

    @AfterEach
    @SneakyThrows
    void destroyMockWebServer() {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void shouldFindStock() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        final FinnhubStockSearchResponseDTO result = stockDataProvider.findStock(STOCK_TICKER);

        assertFindStockResult(result);
    }

    private void assertFindStockResult(final FinnhubStockSearchResponseDTO result) {
        final FinnhubStockSearchDetailsDTO stockSearchResult = result.getResult().get(0);

        final AtomicInteger counter = new AtomicInteger(0);
        final List<FinnhubStockSearchDetailsDTO> resultDetails = result.getResult();

        assertEquals(StockDataProviderTest.FINNHUB_STOCK_SEARCH_RESPONSE.getCount(), result.getCount());

        FINNHUB_STOCK_SEARCH_RESPONSE.getResult().forEach(expectedSearchDetail -> {
            Assertions.assertAll(
                () -> assertEquals(expectedSearchDetail.getDescription(), resultDetails.get(counter.get()).getDescription()),
                () -> assertEquals(expectedSearchDetail.getDisplaySymbol(), resultDetails.get(counter.get()).getDisplaySymbol()),
                () -> assertEquals(expectedSearchDetail.getSymbol(), resultDetails.get(counter.get()).getSymbol()),
                () -> assertEquals(expectedSearchDetail.getType(), resultDetails.get(counter.get()).getType())
            );
            counter.getAndIncrement();
        });
    }

    @Test
    void shouldThrowExceptionWhenFindStockMethodStatusCodeIsError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.findStock(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenFindStockMethodResponseBodyIsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.findStock(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenFindStockMethodResultCountIs0() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-stock-not-found-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.findStock(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenFindStockMethodIsReadTimeout() {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.findStock(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldGetStockData() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(companyProfileJsonResponse));

        final String stockQuoteJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(stockQuoteJsonResponse));

        final StockDataDTO result = stockDataProvider.getStockData(STOCK_TICKER);

        assertStockDataResult(result);
    }

    private void assertStockDataResult(final StockDataDTO result) {
        final StockQuoteDTO resultStockQuote = result.getStockQuote();

        Assertions.assertAll(
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getTicker(), result.getTicker()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCompanyFullName(), result.getCompanyFullName()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCompanyOriginCountry(), result.getCompanyOriginCountry()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getStockExchange(), result.getStockExchange()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getIpoDate(), result.getIpoDate()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getMarketCapitalization(), result.getMarketCapitalization()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getShareOutstanding(), result.getShareOutstanding()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCurrency(), resultStockQuote.getStockCurrency()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getCurrentPrice(), resultStockQuote.getCurrentPrice()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getPriceChange(), resultStockQuote.getPriceChange()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getPercentageChange(), resultStockQuote.getPercentageChange()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getHighestPriceOfTheDay(), resultStockQuote.getHighestPriceOfTheDay()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getLowestPriceOfTheDay(), resultStockQuote.getLowestPriceOfTheDay()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getPreviousClosePrice(), resultStockQuote.getPreviousClosePrice())
        );
    }

    @Test
    void shouldThrowExceptionWhenGetCompanyProfileResponseStatusCodeIsError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetCompanyProfileResponseBodyIsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenCompanyIsNotFound() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-stock-not-found-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetCompanyProfileRequestIsReadTimeout() {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteResponseStatusCodeIsError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteResponseBodyIsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteIsNotFound() {
        final String stockQuoteJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-stock-not-found-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(stockQuoteJsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetStockQuoteRequestIsReadTimeout() {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

}