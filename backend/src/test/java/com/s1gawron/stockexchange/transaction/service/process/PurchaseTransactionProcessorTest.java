package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.TransactionCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionProcessorTest {

    private FinnhubStockDataProvider finnhubStockDataProviderMock;

    private UserWalletService userWalletServiceMock;

    private TransactionDAO transactionDAOMock;

    private PurchaseTransactionProcessor underTest;

    @BeforeEach
    void setUp() {
        finnhubStockDataProviderMock = Mockito.mock(FinnhubStockDataProvider.class);
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAOMock = Mockito.mock(TransactionDAO.class);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeProcessed() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE);
        underTest = new PurchaseTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("22.00"));

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);

        final boolean result = underTest.canProcessTransaction();
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenProvidedTransactionHasWrongTypeWhileCheck() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.SELL);
        underTest = new PurchaseTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("22.00"));

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);

        assertThrows(WrongTransactionTypeForProcessingException.class, () -> underTest.canProcessTransaction());
    }

    @Test
    void shouldReturnFalseWhenCurrentStockPriceIsHigherThanTransactionPriceWhileCheck() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE);
        underTest = new PurchaseTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("180.59"));

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);

        final boolean result = underTest.canProcessTransaction();
        assertFalse(result);
    }

    @Test
    void shouldProcessTransactionWithUserStockPresentAndPurchasePriceSameAsOnTransaction() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserStock> appleUserStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1, 10, new BigDecimal("30.00")));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("60.00"));
        underTest = new PurchaseTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(appleUserStock);
        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction();

        assertEquals(new BigDecimal("45.00"), appleUserStock.get().getAveragePurchasePrice());
        assertEquals(20, appleUserStock.get().getQuantityAvailable());
        assertEquals(new BigDecimal("400.00"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.00"), userWallet.get().getBalanceBlocked());
        assertEquals(new BigDecimal("400.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }

    @Test
    void shouldProcessTransactionWithUserStockPresentAndPurchasePriceLowerThanTransactionPrice() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserStock> appleUserStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1, 10, new BigDecimal("30.00")));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("45.00"));
        underTest = new PurchaseTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(appleUserStock);
        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction();

        assertEquals(new BigDecimal("37.50"), appleUserStock.get().getAveragePurchasePrice());
        assertEquals(20, appleUserStock.get().getQuantityAvailable());
        assertEquals(new BigDecimal("550.00"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.00"), userWallet.get().getBalanceBlocked());
        assertEquals(new BigDecimal("550.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }

    @Test
    void shouldProcessTransactionWithUserStockNotPresentAndPurchasePriceSameAsOnTransaction() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("60.00"));
        underTest = new PurchaseTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction();

        final ArgumentCaptor<UserStock> newUserStockCaptor = ArgumentCaptor.forClass(UserStock.class);
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).saveUserStock(newUserStockCaptor.capture());

        assertEquals(new BigDecimal("60.00"), newUserStockCaptor.getValue().getAveragePurchasePrice());
        assertEquals(10, newUserStockCaptor.getValue().getQuantityAvailable());
        assertEquals(new BigDecimal("400.00"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.00"), userWallet.get().getBalanceBlocked());
        assertEquals(new BigDecimal("400.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }

    @Test
    void shouldProcessTransactionWithUserStockNotPresentAndPurchasePriceLowerThanTransactionPrice() {
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("45.00"));
        underTest = new PurchaseTransactionProcessor(transaction, finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(finnhubStockDataProviderMock.getStockData(transaction.getTransactionPosition().getStockTicker())).thenReturn(appleStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction();

        final ArgumentCaptor<UserStock> newUserStockCaptor = ArgumentCaptor.forClass(UserStock.class);
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).saveUserStock(newUserStockCaptor.capture());

        assertEquals(new BigDecimal("45.00"), newUserStockCaptor.getValue().getAveragePurchasePrice());
        assertEquals(10, newUserStockCaptor.getValue().getQuantityAvailable());
        assertEquals(new BigDecimal("550.00"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.00"), userWallet.get().getBalanceBlocked());
        assertEquals(new BigDecimal("550.00"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(0, ChronoUnit.DAYS.between(LocalDateTime.now(), transaction.getTransactionProcessDate()));
    }
}