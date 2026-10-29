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
import com.s1gawron.stockexchange.user.model.UserStockPosition;
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

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, new BigDecimal("10"),
            new BigDecimal("25.00"));

        assertThrows(TransactionProcessingException.class, () -> underTest.canProcessTransaction(transaction, currentPrice));
    }

    @Test
    void shouldProcessTransactionWithUserHasStockAfterTransactionAndSellPriceSameAsOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("25.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, new BigDecimal("10"),
            new BigDecimal("25.00"));
        final UserStockPosition position = getApplePosition("90", "10", 0);
        final UserWallet userWallet = mockUserStockAndWallet(transaction, UserStockGeneratorHelper.I.getUserStock(position));

        underTest.processTransaction(transaction, currentPrice);

        assertEquals(new BigDecimal("90.000000"), position.getQuantityAvailable());
        assertEquals(new BigDecimal("0.000000"), position.getQuantityBlocked());
        Mockito.verify(userWalletServiceMock).updateUserStockPosition(position);
        Mockito.verify(userWalletServiceMock, Mockito.never()).deleteUserStockPosition(Mockito.any());
        assertEquals(new BigDecimal("7750.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("7750.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionWithUserHasStockAfterTransactionAndSellPriceHigherThanOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("50.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, new BigDecimal("10"),
            new BigDecimal("25.00"));
        final UserStockPosition position = getApplePosition("80", "20", 0);
        final UserWallet userWallet = mockUserStockAndWallet(transaction, UserStockGeneratorHelper.I.getUserStock(position));

        underTest.processTransaction(transaction, currentPrice);

        assertEquals(new BigDecimal("80.000000"), position.getQuantityAvailable());
        assertEquals(new BigDecimal("10.000000"), position.getQuantityBlocked());
        assertEquals(new BigDecimal("8000.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("8000.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionWithUserHasNoStockAfterTransactionAndSellPriceSameAsOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("25.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, new BigDecimal("10"),
            new BigDecimal("25.00"));
        final UserStockPosition position = getApplePosition("0", "10", 0);
        final UserWallet userWallet = mockUserStockAndWallet(transaction, UserStockGeneratorHelper.I.getUserStock(position));

        underTest.processTransaction(transaction, currentPrice);

        Mockito.verify(userWalletServiceMock, Mockito.times(1)).deleteUserStockPosition(position);
        assertEquals(new BigDecimal("7750.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("7750.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionWithUserHasNoStockAfterTransactionAndSellPriceHigherThanOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("50.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, new BigDecimal("20"),
            new BigDecimal("25.00"));
        final UserStockPosition position = getApplePosition("0", "20", 0);
        final UserWallet userWallet = mockUserStockAndWallet(transaction, UserStockGeneratorHelper.I.getUserStock(position));

        underTest.processTransaction(transaction, currentPrice);

        Mockito.verify(userWalletServiceMock, Mockito.times(1)).deleteUserStockPosition(position);
        assertEquals(new BigDecimal("8500.0000"), userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("8500.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldReleaseBlockedStockFromOldestPositionsFirstAndDeleteEmptyPositions() {
        final BigDecimal currentPrice = new BigDecimal("30.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, new BigDecimal("7.5"),
            new BigDecimal("25.00"));
        final UserStockPosition olderPosition = getApplePosition("0", "5", 1);
        final UserStockPosition newerPosition = getApplePosition("17.5", "2.5", 2);
        final UserWallet userWallet = mockUserStockAndWallet(transaction, UserStockGeneratorHelper.I.getUserStock(newerPosition, olderPosition));

        underTest.processTransaction(transaction, currentPrice);

        Mockito.verify(userWalletServiceMock).deleteUserStockPosition(olderPosition);
        Mockito.verify(userWalletServiceMock).updateUserStockPosition(newerPosition);
        assertEquals(new BigDecimal("17.500000"), newerPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("0.000000"), newerPosition.getQuantityBlocked());
        assertEquals(new BigDecimal("7725.0000"), userWallet.getBalanceAvailable());
    }

    @Test
    void shouldReleaseBlockedStockOnlyFromPositionWithBlockedStock() {
        final BigDecimal currentPrice = new BigDecimal("30.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransaction(TransactionType.SELL, new BigDecimal("2"),
            new BigDecimal("25.00"));
        final UserStockPosition olderPosition = getApplePosition("5", "0", 1);
        final UserStockPosition newerPosition = getApplePosition("8", "2", 2);
        mockUserStockAndWallet(transaction, UserStockGeneratorHelper.I.getUserStock(olderPosition, newerPosition));

        underTest.processTransaction(transaction, currentPrice);

        Mockito.verify(userWalletServiceMock, Mockito.never()).updateUserStockPosition(olderPosition);
        Mockito.verify(userWalletServiceMock).updateUserStockPosition(newerPosition);
        assertEquals(new BigDecimal("5.000000"), olderPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("8.000000"), newerPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("0.000000"), newerPosition.getQuantityBlocked());
    }

    private UserWallet mockUserStockAndWallet(final Transaction transaction, final UserStock userStock) {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("7500.00"), new BigDecimal("10000.00"));

        Mockito.when(userWalletServiceMock.getUserStock(transaction.getWalletId(), transaction.getTransactionPosition().getStockTicker()))
            .thenReturn(Optional.of(userStock));
        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(Optional.of(userWallet));

        return userWallet;
    }

    private UserStockPosition getApplePosition(final String quantityAvailable, final String quantityBlocked, final int openDayOffset) {
        return UserStockGeneratorHelper.I.getAppleUserStockPosition(1, new BigDecimal(quantityAvailable), new BigDecimal(quantityBlocked),
            new BigDecimal("25.00"), UserStockGeneratorHelper.OPEN_DATE.plusDays(openDayOffset));
    }

}
