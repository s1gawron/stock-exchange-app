package com.s1gawron.stockexchange.stock.dataprovider;

import com.s1gawron.stockexchange.stock.dataprovider.dto.*;
import com.s1gawron.stockexchange.stock.dataprovider.exception.FinnhubConnectionFailedException;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
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

import java.io.IOException;
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
    void destroyMockWebServer() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldFindStock() throws IOException {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        final FinnhubStockSearchResponseDTO result = stockDataProvider.findStock(STOCK_TICKER);

        assertFindStockResult(result);
    }

    private void assertFindStockResult(final FinnhubStockSearchResponseDTO result) {
        final FinnhubStockSearchDetailsDTO stockSearchResult = result.result().get(0);

        final AtomicInteger counter = new AtomicInteger(0);
        final List<FinnhubStockSearchDetailsDTO> resultDetails = result.result();

        assertEquals(StockDataProviderTest.FINNHUB_STOCK_SEARCH_RESPONSE.count(), result.count());

        FINNHUB_STOCK_SEARCH_RESPONSE.result().forEach(expectedSearchDetail -> {
            Assertions.assertAll(
                () -> assertEquals(expectedSearchDetail.description(), resultDetails.get(counter.get()).description()),
                () -> assertEquals(expectedSearchDetail.displaySymbol(), resultDetails.get(counter.get()).displaySymbol()),
                () -> assertEquals(expectedSearchDetail.symbol(), resultDetails.get(counter.get()).symbol()),
                () -> assertEquals(expectedSearchDetail.type(), resultDetails.get(counter.get()).type())
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
    void shouldThrowExceptionWhenFindStockMethodResultCountIs0() throws IOException {
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
    void shouldGetStockData() throws IOException {
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
        final StockQuoteDTO resultStockQuote = result.stockQuote();

        Assertions.assertAll(
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.ticker(), result.ticker()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.companyFullName(), result.companyFullName()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.companyOriginCountry(), result.companyOriginCountry()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.stockExchange(), result.stockExchange()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.ipoDate(), result.ipoDate()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.marketCapitalization(), result.marketCapitalization()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.shareOutstanding(), result.shareOutstanding()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.currency(), resultStockQuote.stockCurrency()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.currentPrice(), resultStockQuote.currentPrice()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.priceChange(), resultStockQuote.priceChange()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.percentageChange(), resultStockQuote.percentageChange()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.highestPriceOfTheDay(), resultStockQuote.highestPriceOfTheDay()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.lowestPriceOfTheDay(), resultStockQuote.lowestPriceOfTheDay()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.previousClosePrice(), resultStockQuote.previousClosePrice())
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
    void shouldThrowExceptionWhenCompanyIsNotFound() throws IOException {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-stock-not-found-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetCompanyProfileRequestIsReadTimeout() {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetStockQuoteResponseStatusCodeIsError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetStockQuoteResponseBodyIsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetStockQuoteIsNotFound() throws IOException {
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