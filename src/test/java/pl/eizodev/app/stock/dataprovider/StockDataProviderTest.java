package pl.eizodev.app.stock.dataprovider;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchDetailsDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dto.StockDataDTO;
import pl.eizodev.app.stock.dto.StockQuoteDTO;
import pl.eizodev.app.stock.exception.FinnhubConnectionFailedException;
import pl.eizodev.app.stock.exception.StockNotFoundException;
import pl.eizodev.app.stock.model.StockCompanyDetails;
import pl.eizodev.app.stock.repository.StockCompanyDetailsRepository;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class StockDataProviderTest {

    private static final String STOCK_TICKER = "AAPL";

    private static final FinnhubStockSearchDetailsDTO FINNHUB_STOCK_SEARCH_DETAILS_RESPONSE = new FinnhubStockSearchDetailsDTO("APPLE INC", "AAPL", "AAPL",
        "Common Stock");

    private static final FinnhubCompanyProfileResponseDTO COMPANY_PROFILE_RESPONSE = new FinnhubCompanyProfileResponseDTO(STOCK_TICKER, "Apple Inc", "US",
        "NASDAQ NMS - GLOBAL MARKET", "Technology", "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, "USD");

    private static final FinnhubStockQuoteResponseDTO STOCK_QUOTE_RESPONSE = new FinnhubStockQuoteResponseDTO(BigDecimal.valueOf(159.59),
        BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015), BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05),
        BigDecimal.valueOf(155.09), 1647460804);

    private MockWebServer mockWebServer;

    private StockDataProvider stockDataProvider;

    private StockCompanyDetailsRepository stockCompanyDetailsRepositoryMock;

    @BeforeEach
    void setupMockWebServer() {
        mockWebServer = new MockWebServer();
        stockCompanyDetailsRepositoryMock = Mockito.mock(StockCompanyDetailsRepository.class);

        final FinnhubConnectionFactory finnhubConnectionFactory = new FinnhubConnectionFactory("test", mockWebServer.url("/api/v1").url().toString());
        stockDataProvider = new StockDataProvider(stockCompanyDetailsRepositoryMock, finnhubConnectionFactory);
    }

    @AfterEach
    @SneakyThrows
    void destroyMockWebServer() {
        mockWebServer.shutdown();
    }

    @Test
    @SneakyThrows
    void shouldFindStock() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/symbol-lookup-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        final FinnhubStockSearchResponseDTO result = stockDataProvider.findStock(STOCK_TICKER);

        assertFindStockResult(result);
    }

    private void assertFindStockResult(final FinnhubStockSearchResponseDTO result) {
        final FinnhubStockSearchDetailsDTO stockSearchResult = result.getResult().get(0);

        Assertions.assertAll(
            () -> assertEquals(4, result.getCount()),
            () -> assertEquals(FINNHUB_STOCK_SEARCH_DETAILS_RESPONSE.getDescription(), stockSearchResult.getDescription()),
            () -> assertEquals(FINNHUB_STOCK_SEARCH_DETAILS_RESPONSE.getDisplaySymbol(), stockSearchResult.getDisplaySymbol()),
            () -> assertEquals(FINNHUB_STOCK_SEARCH_DETAILS_RESPONSE.getSymbol(), stockSearchResult.getSymbol()),
            () -> assertEquals(FINNHUB_STOCK_SEARCH_DETAILS_RESPONSE.getType(), stockSearchResult.getType())
        );

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
        final String jsonResponse = Files.readString(Path.of("src/test/resources/empty-symbol-lookup-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.findStock(STOCK_TICKER));
    }

    @Test
    void shouldGetStockDataFromDatabase() {

        final StockCompanyDetails stockCompanyDetails = StockCompanyDetails.createFrom(COMPANY_PROFILE_RESPONSE, STOCK_QUOTE_RESPONSE);
        final Optional<StockCompanyDetails> stockCompanyDetailsOptional = Optional.of(stockCompanyDetails);

        Mockito.when(stockCompanyDetailsRepositoryMock.findByTicker(STOCK_TICKER)).thenReturn(stockCompanyDetailsOptional);

        final StockDataDTO result = stockDataProvider.getStockData(STOCK_TICKER);

        Assertions.assertNotNull(result);
    }

    @Test
    @SneakyThrows
    void shouldGetStockDataFromAPI() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/company-profile-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(companyProfileJsonResponse));

        final String stockQuoteJsonResponse = Files.readString(Path.of("src/test/resources/stock-quote-response.json"));

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
    void shouldThrowExceptionWhenGetCompanyProfileMethodStatusCodeIsError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteMethodStatusCodeIsError() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/company-profile-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(companyProfileJsonResponse));

        mockWebServer.enqueue(new MockResponse().setResponseCode(502)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(FinnhubConnectionFailedException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenGetCompanyProfilMethodeResponseBodyIsNull() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteMethodResponseBodyIsNull() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/company-profile-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(companyProfileJsonResponse));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetCompanyProfileMethodStockIsNotFound() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/company-profile-stock-not-found-result.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(jsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenGetStockQuoteMethodStockIsNotFound() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/company-profile-response.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(companyProfileJsonResponse));

        final String stockQuoteJsonResponse = Files.readString(Path.of("src/test/resources/stock-quote-stock-not-found-result.json"));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(stockQuoteJsonResponse));

        Assertions.assertThrows(StockNotFoundException.class, () -> stockDataProvider.getStockData(STOCK_TICKER));
    }

}