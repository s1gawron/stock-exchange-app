package pl.eizodev.app.stock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import pl.eizodev.app.stock.dataprovider.StockDataProvider;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchDetailsDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dto.StockDataDTO;
import pl.eizodev.app.stock.dto.StockQuoteDTO;
import pl.eizodev.app.stock.entity.StockCompanyDetails;
import pl.eizodev.app.stock.entity.StockQuote;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockDataProviderServiceTest {

    private static final String STOCK_TICKER = "AAPL";

    private static final FinnhubStockSearchDetailsDTO FINNHUB_STOCK_SEARCH_DETAILS_RESPONSE = new FinnhubStockSearchDetailsDTO("APPLE INC", "AAPL", "AAPL",
        "Common Stock");

    private static final FinnhubCompanyProfileResponseDTO COMPANY_PROFILE_RESPONSE = new FinnhubCompanyProfileResponseDTO(STOCK_TICKER, "Apple Inc", "US",
        "NASDAQ NMS - GLOBAL MARKET", "Technology", "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, "USD", "https://finnhub.io/api/logo?symbol=AAPL",
        "14089961010.0", "https://www.apple.com/");

    private static final FinnhubStockQuoteResponseDTO STOCK_QUOTE_RESPONSE = new FinnhubStockQuoteResponseDTO(BigDecimal.valueOf(159.59),
        BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015), BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05),
        BigDecimal.valueOf(155.09), 1647460804);

    private final ObjectMapper mapper = new ObjectMapper();

    private StockDataProvider stockDataProviderMock;

    private StockCompanyDetailsService stockCompanyDetailsServiceMock;

    private StockDataProviderService stockDataProviderService;

    @BeforeEach
    void setUp() {
        stockDataProviderMock = Mockito.mock(StockDataProvider.class);
        stockCompanyDetailsServiceMock = Mockito.mock(StockCompanyDetailsService.class);
        stockDataProviderService = new StockDataProviderService(stockDataProviderMock, stockCompanyDetailsServiceMock);
    }

    @Test
    @SneakyThrows
    void shouldFindStock() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-search-response.json"));
        final FinnhubStockSearchResponseDTO response = mapper.readValue(jsonResponse, FinnhubStockSearchResponseDTO.class);

        Mockito.when(stockDataProviderMock.findStock(STOCK_TICKER)).thenReturn(response);

        final FinnhubStockSearchResponseDTO result = stockDataProviderService.findStock(STOCK_TICKER);

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
    void shouldGetStockDataFromDatabase() {
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(1647460804), TimeZone.getDefault().toZoneId());
        final StockQuote stockQuote = new StockQuote(STOCK_TICKER, "USD", BigDecimal.valueOf(159.59), BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015),
            BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05), BigDecimal.valueOf(155.09), lastUpdateDate);
        final StockCompanyDetails stockCompanyDetails = new StockCompanyDetails(STOCK_TICKER, "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology",
            "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, 0, stockQuote);

        ReflectionTestUtils.setField(stockCompanyDetails, "id", 1);
        Mockito.when(stockCompanyDetailsServiceMock.getStockByTicker(STOCK_TICKER)).thenReturn(Optional.of(stockCompanyDetails));
        Mockito.when(stockCompanyDetailsServiceMock.updateNumberOfInvokes(1)).thenReturn(stockCompanyDetails);

        final StockDataDTO result = stockDataProviderService.getStockData(STOCK_TICKER);

        assertStockDataResult(result);
    }

    @Test
    @SneakyThrows
    void shouldGetStockDataFromStockDataProvider() {
        final String companyProfileJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-company-profile-response.json"));
        final FinnhubCompanyProfileResponseDTO companyProfileResponse = mapper.readValue(companyProfileJsonResponse, FinnhubCompanyProfileResponseDTO.class);
        final String stockQuoteJsonResponse = Files.readString(Path.of("src/test/resources/finnhub-stock-quote-response.json"));
        final FinnhubStockQuoteResponseDTO stockQuoteResponse = mapper.readValue(stockQuoteJsonResponse, FinnhubStockQuoteResponseDTO.class);

        Mockito.when(stockDataProviderMock.getCompanyProfile(STOCK_TICKER)).thenReturn(companyProfileResponse);
        Mockito.when(stockDataProviderMock.getStockQuote(STOCK_TICKER)).thenReturn(stockQuoteResponse);

        final StockDataDTO result = stockDataProviderService.getStockData(STOCK_TICKER);

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

}