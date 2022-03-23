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
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchDetailsDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
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

        final FinnhubConnectionFactory finnhubConnectionFactory = new FinnhubConnectionFactory("test", mockWebServer.url("/api/v1").url().toString());
        stockDataProvider = new StockDataProvider(finnhubConnectionFactory);
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

        StockDataProviderTest.FINNHUB_STOCK_SEARCH_RESPONSE.getResult().forEach(expectedSearchDetail -> {
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
    void shouldGetCompanyProfile() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(companyProfileJsonResponse));

        final FinnhubCompanyProfileResponseDTO response = stockDataProvider.getCompanyProfile(STOCK_TICKER);

        assertFinnhubCompanyProfileResponse(response);
    }

    private void assertFinnhubCompanyProfileResponse(final FinnhubCompanyProfileResponseDTO response) {
        Assertions.assertAll(
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getTicker(), response.getTicker()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCompanyFullName(), response.getCompanyFullName()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCompanyOriginCountry(), response.getCompanyOriginCountry()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getStockExchange(), response.getStockExchange()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getIpoDate(), response.getIpoDate()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getMarketCapitalization(), response.getMarketCapitalization()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getShareOutstanding(), response.getShareOutstanding()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCurrency(), response.getCurrency()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCompanyLogoUrl(), response.getCompanyLogoUrl()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCompanyPhone(), response.getCompanyPhone()),
            () -> assertEquals(COMPANY_PROFILE_RESPONSE.getCompanyWebsiteUrl(), response.getCompanyWebsiteUrl())
        );
    }

    @Test
    void shouldThrowExceptionWhenGetCompanyProfileMethodStatusCodeIsError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getCompanyProfile(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetCompanyProfilMethodeResponseBodyIsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getCompanyProfile(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetCompanyProfileMethodStockIsNotFound() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-stock-not-found-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getCompanyProfile(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetCompanyProfileMethodIsReadTimeout() {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getCompanyProfile(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldGetStockQuote() {
        final String stockQuoteJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(stockQuoteJsonResponse));

        final FinnhubStockQuoteResponseDTO response = stockDataProvider.getStockQuote(STOCK_TICKER);

        assertFinnhubStockQuoteResponse(response);
    }

    private void assertFinnhubStockQuoteResponse(final FinnhubStockQuoteResponseDTO response) {
        Assertions.assertAll(
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getCurrentPrice(), response.getCurrentPrice()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getPriceChange(), response.getPriceChange()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getPercentageChange(), response.getPercentageChange()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getHighestPriceOfTheDay(), response.getHighestPriceOfTheDay()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getLowestPriceOfTheDay(), response.getLowestPriceOfTheDay()),
            () -> assertEquals(STOCK_QUOTE_RESPONSE.getPreviousClosePrice(), response.getPreviousClosePrice())
        );
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteMethodStatusCodeIsError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockQuote(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteMethodResponseBodyIsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockQuote(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteMethodStockIsNotFound() {
        final String stockQuoteJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-stock-not-found-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(stockQuoteJsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockQuote(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetStockQuoteMethodIsReadTimeout() {
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockQuote(STOCK_TICKER));
    }

}