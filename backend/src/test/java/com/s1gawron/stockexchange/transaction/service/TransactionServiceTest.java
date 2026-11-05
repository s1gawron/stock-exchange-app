package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockQuoteDTO;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.transaction.dto.TransactionModifyRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
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
    void shouldFetchStockDataOnceAndPassCurrencyToTransactionExecution() {
        final BigDecimal currentPrice = new BigDecimal("45.00");
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(StockDataGeneratorHelper.I.getAppleStock(currentPrice));

        underTest.validateAndCreateTransaction(TRANSACTION_REQUEST_DTO);

        Mockito.verify(stockDataProviderMock, Mockito.times(1)).getStockData("AAPL");
        Mockito.verify(transactionExecutionServiceMock).createTransaction(TRANSACTION_REQUEST_DTO, "USD");
    }

    @Test
    void shouldThrowExceptionWhenCurrentStockPriceIsZero() {
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("0.00")));

        assertThrows(TransactionProcessingException.class, () -> underTest.validateAndCreateTransaction(TRANSACTION_REQUEST_DTO));
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

        assertThrows(TransactionProcessingException.class, () -> underTest.validateAndCreateTransaction(TRANSACTION_REQUEST_DTO));
        Mockito.verifyNoInteractions(transactionExecutionServiceMock);
    }

    @Test
    void shouldThrowExceptionWhenStockIsNotFound() {
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenThrow(StockNotFoundException.createFromTicker("AAPL"));

        assertThrows(StockNotFoundException.class, () -> underTest.validateAndCreateTransaction(TRANSACTION_REQUEST_DTO));
        Mockito.verifyNoInteractions(transactionExecutionServiceMock);
    }

    @Test
    void shouldNotFetchStockDataWhenTransactionRequestIsInvalid() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, null, new BigDecimal("60.00"), new BigDecimal("10"));

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> underTest.validateAndCreateTransaction(transactionRequestDTO));
        Mockito.verifyNoInteractions(stockDataProviderMock, transactionExecutionServiceMock);
    }

    @Test
    void shouldModifyTransactionWithoutFetchingStockData() {
        underTest.validateAndModifyTransaction(3, new TransactionModifyRequestDTO(new BigDecimal("55.00"), new BigDecimal("4")));

        Mockito.verify(transactionExecutionServiceMock).modifyTransaction(3, new BigDecimal("55.00"), new BigDecimal("4"));
        Mockito.verifyNoInteractions(stockDataProviderMock);
    }

    @Test
    void shouldNotModifyTransactionWhenNewPriceIsNotPositive() {
        final TransactionModifyRequestDTO transactionModifyRequestDTO = new TransactionModifyRequestDTO(new BigDecimal("0"), new BigDecimal("4"));

        assertThrows(StockPriceLteZeroException.class, () -> underTest.validateAndModifyTransaction(3, transactionModifyRequestDTO));
        Mockito.verifyNoInteractions(transactionExecutionServiceMock);
    }

    @Test
    void shouldNotModifyTransactionWhenNewQuantityIsMissing() {
        final TransactionModifyRequestDTO transactionModifyRequestDTO = new TransactionModifyRequestDTO(new BigDecimal("55.00"), null);

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> underTest.validateAndModifyTransaction(3, transactionModifyRequestDTO));
        Mockito.verifyNoInteractions(transactionExecutionServiceMock);
    }

}
