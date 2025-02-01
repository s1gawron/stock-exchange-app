package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.TransactionCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SellTransactionProcessorTest {

    private FinnhubStockDataProvider finnhubStockDataProviderMock;

    private UserWalletService userWalletServiceMock;

    private TransactionDAO transactionDAOMock;

    private SellTransactionProcessor underTest;

    @BeforeEach
    void setUp() {
        finnhubStockDataProviderMock = Mockito.mock(FinnhubStockDataProvider.class);
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAOMock = Mockito.mock(TransactionDAO.class);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeProcessed() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("25.00"));
        underTest = new SellTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);

        final boolean result = underTest.canProcessTransaction();
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenProvidedTransactionHasWrongTypeWhileCheck() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("25.00"));
        underTest = new SellTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);

        assertThrows(WrongTransactionTypeForProcessingException.class, () -> underTest.canProcessTransaction());
    }

    @Test
    void shouldReturnFalseWhenCurrentStockPriceIsHigherThanTransactionPriceWhileCheck() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, 10, new BigDecimal("25.00"));
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("16.50"));
        underTest = new SellTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);

        assertThrows(TransactionProcessingException.class, () -> underTest.canProcessTransaction());
    }

    @Test
    void shouldProcessTransactionWithUserHasStockAfterTransactionAndSellPriceSameAsOnTransaction() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 10, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 10);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("25.00"));
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));
        underTest = new SellTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction();

        assertEquals(90, userStock.getQuantityAvailable());
        assertEquals(0, userStock.getQuantityBlocked());
        assertEquals(new BigDecimal("7750.00"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("7750.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }

    @Test
    void shouldProcessTransactionWithUserHasStockAfterTransactionAndSellPriceHigherThanOnTransaction() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 10, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 20);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("50.00"));
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));
        underTest = new SellTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction();

        assertEquals(80, userStock.getQuantityAvailable());
        assertEquals(10, userStock.getQuantityBlocked());
        assertEquals(new BigDecimal("8000.00"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("8000.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }

    @Test
    void shouldProcessTransactionWithUserHasNoStockAfterTransactionAndSellPriceSameAsOnTransaction() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 10, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 10, 10);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("25.00"));
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));
        underTest = new SellTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction();

        Mockito.verify(userWalletServiceMock, Mockito.times(1)).deleteUserStock(userStock);
        assertEquals(new BigDecimal("7750.00"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("7750.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }

    @Test
    void shouldProcessTransactionWithUserHasNoStockAfterTransactionAndSellPriceHigherThanOnTransaction() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 20, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 20, 20);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("50.00"));
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));
        underTest = new SellTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction();

        Mockito.verify(userWalletServiceMock, Mockito.times(1)).deleteUserStock(userStock);
        assertEquals(new BigDecimal("8500.00"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("8500.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }

}