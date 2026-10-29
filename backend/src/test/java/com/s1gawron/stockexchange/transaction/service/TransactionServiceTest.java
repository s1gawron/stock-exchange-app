package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockQuoteDTO;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import com.s1gawron.stockexchange.transaction.exception.TransactionRequestEmptyPropertiesException;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionServiceTest {

    private static final TransactionRequestDTO TRANSACTION_REQUEST_DTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("60.00"),
        new BigDecimal("10"));

    private TransactionExecutionService transactionExecutionServiceMock;

    private StockDataProvider stockDataProviderMock;

    private TransactionService underTest;

    @BeforeEach
    void setUp() {
        transactionExecutionServiceMock = Mockito.mock(TransactionExecutionService.class);
        stockDataProviderMock = Mockito.mock(StockDataProvider.class);
        underTest = new TransactionService(transactionExecutionServiceMock, stockDataProviderMock);
    }

    @Test
    void shouldFetchStockPriceOnceAndPassItToTransactionExecution() {
        final BigDecimal currentPrice = new BigDecimal("45.00");
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(StockDataGeneratorHelper.I.getAppleStock(currentPrice));

        underTest.validateCreateAndProcessTransaction(TRANSACTION_REQUEST_DTO);

        Mockito.verify(stockDataProviderMock, Mockito.times(1)).getStockData("AAPL");
        Mockito.verify(transactionExecutionServiceMock).createAndProcessTransaction(TRANSACTION_REQUEST_DTO, currentPrice, "USD");
    }

    @Test
    void shouldThrowExceptionWhenCurrentStockPriceIsZero() {
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("0.00")));

        assertThrows(TransactionProcessingException.class, () -> underTest.validateCreateAndProcessTransaction(TRANSACTION_REQUEST_DTO));
        Mockito.verifyNoInteractions(transactionExecutionServiceMock);
    }

    @Test
    void shouldThrowExceptionWhenStockCurrencyIsNotAvailable() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("45.00"));
        final StockQuoteDTO quote = appleStock.stockQuote();
        final StockQuoteDTO quoteWithoutCurrency = new StockQuoteDTO(null, quote.currentPrice(), quote.priceChange(), quote.percentagePriceChange(),
            quote.highestPriceOfTheDay(), quote.lowestPriceOfTheDay(), quote.openPriceOfTheDay(), quote.previousClosePrice());
        final StockDataDTO appleStockWithoutCurrency = new StockDataDTO(appleStock.ticker(), appleStock.companyFullName(), appleStock.companyOriginCountry(),
            appleStock.stockExchange(), appleStock.companyIndustry(), appleStock.ipoDate(), appleStock.marketCapitalization(), appleStock.shareOutstanding(),
            quoteWithoutCurrency, appleStock.lastUpdateDate());

        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(appleStockWithoutCurrency);

        assertThrows(TransactionProcessingException.class, () -> underTest.validateCreateAndProcessTransaction(TRANSACTION_REQUEST_DTO));
        Mockito.verifyNoInteractions(transactionExecutionServiceMock);
    }

    @Test
    void shouldThrowExceptionWhenStockIsNotFound() {
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenThrow(StockNotFoundException.createFromTicker("AAPL"));

        assertThrows(StockNotFoundException.class, () -> underTest.validateCreateAndProcessTransaction(TRANSACTION_REQUEST_DTO));
        Mockito.verifyNoInteractions(transactionExecutionServiceMock);
    }

    @Test
    void shouldNotFetchStockDataWhenTransactionRequestIsInvalid() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, null, new BigDecimal("60.00"), new BigDecimal("10"));

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> underTest.validateCreateAndProcessTransaction(transactionRequestDTO));
        Mockito.verifyNoInteractions(stockDataProviderMock, transactionExecutionServiceMock);
    }

}
