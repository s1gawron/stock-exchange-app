package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.helper.TransactionCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
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
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SellTransactionProcessorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private UserWalletService userWalletServiceMock;

    private InMemoryTransactionDAO transactionDAO;

    private SellTransactionProcessor underTest;

    @BeforeEach
    void setUp() {
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAO = new InMemoryTransactionDAO();
        underTest = new SellTransactionProcessor(userWalletServiceMock, transactionDAO, CLOCK);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeProcessed() {
        final BigDecimal currentPrice = new BigDecimal("25.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL);

        final boolean result = underTest.canProcessTransaction(transaction, currentPrice);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenProvidedTransactionHasWrongTypeWhileCheck() {
        final BigDecimal currentPrice = new BigDecimal("25.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE);

        assertThrows(WrongTransactionTypeForProcessingException.class, () -> underTest.canProcessTransaction(transaction, currentPrice));
    }

    @Test
    void shouldReturnFalseWhenCurrentStockPriceIsHigherThanTransactionPriceWhileCheck() {
        final BigDecimal currentPrice = new BigDecimal("16.50");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, 10, new BigDecimal("25.00"));

        assertThrows(TransactionProcessingException.class, () -> underTest.canProcessTransaction(transaction, currentPrice));
    }

    @Test
    void shouldProcessTransactionWithUserHasStockAfterTransactionAndSellPriceSameAsOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("25.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 10, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction(transaction, currentPrice);

        assertEquals(90, userStock.getQuantityAvailable());
        assertEquals(0, userStock.getQuantityBlocked());
        assertEquals(new BigDecimal("7750.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("7750.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionWithUserHasStockAfterTransactionAndSellPriceHigherThanOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("50.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 10, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 20);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction(transaction, currentPrice);

        assertEquals(80, userStock.getQuantityAvailable());
        assertEquals(10, userStock.getQuantityBlocked());
        assertEquals(new BigDecimal("8000.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("8000.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionWithUserHasNoStockAfterTransactionAndSellPriceSameAsOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("25.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 10, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 10, 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction(transaction, currentPrice);

        Mockito.verify(userWalletServiceMock, Mockito.times(1)).deleteUserStock(userStock);
        assertEquals(new BigDecimal("7750.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("7750.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionWithUserHasNoStockAfterTransactionAndSellPriceHigherThanOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("50.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.PURCHASE, 20, new BigDecimal("25.00"));
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(transaction.getWalletId(), 20, 20);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        underTest.processTransaction(transaction, currentPrice);

        Mockito.verify(userWalletServiceMock, Mockito.times(1)).deleteUserStock(userStock);
        assertEquals(new BigDecimal("8500.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("8500.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

}