package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.TransactionCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.InMemoryStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.transaction.dao.impl.InMemoryTransactionDAO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionProcessorTest {

    private InMemoryStockDataProvider stockDataProvider;

    private UserWalletService userWalletServiceMock;

    private InMemoryTransactionDAO transactionDAO;

    private PurchaseTransactionProcessor underTest;

    @BeforeEach
    void setUp() {
        stockDataProvider = new InMemoryStockDataProvider();
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAO = new InMemoryTransactionDAO();
        underTest = new PurchaseTransactionProcessor(stockDataProvider, userWalletServiceMock, transactionDAO);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeProcessed() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("22.00"));
        stockDataProvider.addStockData(appleStock);

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE);

        final boolean result = underTest.canProcessTransaction(transaction);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenProvidedTransactionHasWrongTypeWhileCheck() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("22.00"));
        stockDataProvider.addStockData(appleStock);

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.SELL);

        assertThrows(WrongTransactionTypeForProcessingException.class, () -> underTest.canProcessTransaction(transaction));
    }

    @Test
    void shouldThrowExceptionWhenCurrentStockPriceIsHigherThanTransactionPriceWhileCheck() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("180.59"));
        stockDataProvider.addStockData(appleStock);

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE);

        assertThrows(TransactionProcessingException.class, () -> underTest.canProcessTransaction(transaction));
    }

    @Test
    void shouldProcessTransactionWithUserStockPresentAndPurchasePriceSameAsOnTransaction() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("60.00"));
        stockDataProvider.addStockData(appleStock);

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserStock> appleUserStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1, 10, new BigDecimal("30.00")));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(appleUserStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction);

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
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("45.00"));
        stockDataProvider.addStockData(appleStock);

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserStock> appleUserStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1, 10, new BigDecimal("30.00")));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(appleUserStock);
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction);

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
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("60.00"));
        stockDataProvider.addStockData(appleStock);

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );

        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction);

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
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("45.00"));
        stockDataProvider.addStockData(appleStock);

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE, 10,
            new BigDecimal("60.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );

        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction);

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