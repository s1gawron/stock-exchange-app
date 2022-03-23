package pl.eizodev.app.stock.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.eizodev.app.stock.entity.StockCompanyDetails;
import pl.eizodev.app.stock.entity.StockQuote;
import pl.eizodev.app.stock.repository.StockCompanyDetailsRepository;
import pl.eizodev.app.stock.service.exception.StockCompanyDetailsNotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;

class StockCompanyDetailsServiceTest {

    private static final String STOCK_TICKER = "AAPL";

    private StockCompanyDetailsService stockCompanyDetailsService;

    private StockCompanyDetailsRepository stockCompanyDetailsRepositoryMock;

    @BeforeEach
    void setUp() {
        stockCompanyDetailsRepositoryMock = Mockito.mock(StockCompanyDetailsRepository.class);
        stockCompanyDetailsService = new StockCompanyDetailsService(stockCompanyDetailsRepositoryMock);
    }

    @Test
    void shouldGetStockByTicker() {
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(1647460804), TimeZone.getDefault().toZoneId());
        final StockQuote stockQuote = new StockQuote(STOCK_TICKER, "USD", BigDecimal.valueOf(159.59), BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015),
            BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05), BigDecimal.valueOf(155.09), lastUpdateDate);
        final StockCompanyDetails stockCompanyDetails = new StockCompanyDetails(STOCK_TICKER, "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology",
            "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, 0, stockQuote);

        Mockito.when(stockCompanyDetailsRepositoryMock.findByTicker(STOCK_TICKER)).thenReturn(Optional.of(stockCompanyDetails));

        final Optional<StockCompanyDetails> result = stockCompanyDetailsService.getStockByTicker(STOCK_TICKER);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertNotNull(result.get());
    }

    @Test
    void shouldReturnEmptyOptionalWhenStockIsNotFound() {
        Mockito.when(stockCompanyDetailsRepositoryMock.findByTicker(STOCK_TICKER)).thenReturn(Optional.empty());

        final Optional<StockCompanyDetails> result = stockCompanyDetailsService.getStockByTicker(STOCK_TICKER);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveStockCompanyDetails() {
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(1647460804), TimeZone.getDefault().toZoneId());
        final StockQuote stockQuote = new StockQuote(STOCK_TICKER, "USD", BigDecimal.valueOf(159.59), BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015),
            BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05), BigDecimal.valueOf(155.09), lastUpdateDate);
        final StockCompanyDetails stockCompanyDetails = new StockCompanyDetails(STOCK_TICKER, "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology",
            "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, 0, stockQuote);

        stockCompanyDetailsService.saveCompanyDetails(stockCompanyDetails);

        Mockito.verify(stockCompanyDetailsRepositoryMock, Mockito.times(1)).save(stockCompanyDetails);
    }

    @Test
    void shouldUpdateNumberOfInvokes() {
        final LocalDateTime lastUpdateDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(1647460804), TimeZone.getDefault().toZoneId());
        final StockQuote stockQuote = new StockQuote(STOCK_TICKER, "USD", BigDecimal.valueOf(159.59), BigDecimal.valueOf(4.5), BigDecimal.valueOf(2.9015),
            BigDecimal.valueOf(160), BigDecimal.valueOf(154.46), BigDecimal.valueOf(157.05), BigDecimal.valueOf(155.09), lastUpdateDate);
        final StockCompanyDetails stockCompanyDetails = new StockCompanyDetails(STOCK_TICKER, "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology",
            "1980-12-12", BigDecimal.valueOf(2458034), 16319.44, 23, stockQuote);

        Mockito.when(stockCompanyDetailsRepositoryMock.findById(1)).thenReturn(Optional.of(stockCompanyDetails));

        final StockCompanyDetails result = stockCompanyDetailsService.updateNumberOfInvokes(1);

        Assertions.assertEquals(24, result.getNumberOfInvokes());
        Mockito.verify(stockCompanyDetailsRepositoryMock, Mockito.times(1)).save(stockCompanyDetails);
    }

    @Test
    void shouldThrowExceptionWhenStockCompanyDetailsIsNotFound() {
        Mockito.when(stockCompanyDetailsRepositoryMock.findById(1)).thenReturn(Optional.empty());
        Assertions.assertThrows(StockCompanyDetailsNotFoundException.class, () -> stockCompanyDetailsService.updateNumberOfInvokes(1));
    }

}