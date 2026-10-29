package com.s1gawron.stockexchange.transaction.service.process;

import com.s1gawron.stockexchange.shared.helper.TransactionCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.transaction.dao.impl.InMemoryTransactionDAO;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import com.s1gawron.stockexchange.transaction.exception.WrongTransactionTypeForProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionProcessorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private UserWalletService userWalletServiceMock;

    private InMemoryTransactionDAO transactionDAO;

    private PurchaseTransactionProcessor underTest;

    @BeforeEach
    void setUp() {
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAO = new InMemoryTransactionDAO();
        underTest = new PurchaseTransactionProcessor(userWalletServiceMock, transactionDAO, CLOCK);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeProcessed() {
        final BigDecimal currentPrice = new BigDecimal("22.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE);

        final boolean result = underTest.canProcessTransaction(transaction, currentPrice);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenProvidedTransactionHasWrongTypeWhileCheck() {
        final BigDecimal currentPrice = new BigDecimal("22.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.SELL);

        assertThrows(WrongTransactionTypeForProcessingException.class, () -> underTest.canProcessTransaction(transaction, currentPrice));
    }

    @Test
    void shouldThrowExceptionWhenCurrentStockPriceIsHigherThanTransactionPriceWhileCheck() {
        final BigDecimal currentPrice = new BigDecimal("180.59");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE);

        assertThrows(TransactionProcessingException.class, () -> underTest.canProcessTransaction(transaction, currentPrice));
    }

    @Test
    void shouldProcessTransactionAndOpenNewPositionWithPurchasePriceSameAsOnTransaction() {
        final BigDecimal currentPrice = new BigDecimal("60.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE,
            new BigDecimal("10"), new BigDecimal("60.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );

        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction, currentPrice);

        final UserStockPosition newPosition = captureSavedPosition();
        assertEquals("AAPL", newPosition.getTicker());
        assertEquals("USD", newPosition.getCurrency());
        assertEquals(new BigDecimal("60.0000"), newPosition.getOpenPrice());
        assertEquals(new BigDecimal("10.000000"), newPosition.getQuantityAvailable());
        assertEquals(LocalDateTime.now(CLOCK), newPosition.getOpenDate());
        assertEquals(new BigDecimal("400.0000"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.00"), userWallet.get().getBalanceBlocked());
        assertEquals(new BigDecimal("400.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionAndOpenNewPositionWithPurchasePriceLowerThanTransactionPrice() {
        final BigDecimal currentPrice = new BigDecimal("45.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE,
            new BigDecimal("10"), new BigDecimal("60.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("600.00"))
        );

        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction, currentPrice);

        final UserStockPosition newPosition = captureSavedPosition();
        assertEquals(new BigDecimal("45.0000"), newPosition.getOpenPrice());
        assertEquals(new BigDecimal("10.000000"), newPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("550.0000"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.00"), userWallet.get().getBalanceBlocked());
        assertEquals(new BigDecimal("550.0000"), transaction.getBalanceAfterTransaction());
        assertEquals(TransactionStatus.COMPLETED, transaction.getTransactionStatus());
        assertEquals(LocalDateTime.now(CLOCK), transaction.getTransactionProcessDate());
    }

    @Test
    void shouldProcessTransactionWithFractionalQuantity() {
        final BigDecimal currentPrice = new BigDecimal("45.00");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE,
            new BigDecimal("0.25"), new BigDecimal("60.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("15.00"))
        );

        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction, currentPrice);

        final UserStockPosition newPosition = captureSavedPosition();
        assertEquals(new BigDecimal("0.250000"), newPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("988.7500"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.0000"), userWallet.get().getBalanceBlocked());
    }

    @Test
    void shouldRoundTransactionCostAndOpenPriceWhenCurrentPriceHasMoreDecimalPlaces() {
        final BigDecimal currentPrice = new BigDecimal("33.33333");

        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE,
            new BigDecimal("3"), new BigDecimal("34.00"));
        final Optional<UserWallet> userWallet = Optional.of(
            UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("1000.00"), new BigDecimal("1000.00"), new BigDecimal("102.00"))
        );

        Mockito.when(userWalletServiceMock.getUserWallet(transaction.getWalletId())).thenReturn(userWallet);

        underTest.processTransaction(transaction, currentPrice);

        assertEquals(new BigDecimal("33.3333"), captureSavedPosition().getOpenPrice());
        assertEquals(new BigDecimal("900.0000"), userWallet.get().getBalanceAvailable());
        assertEquals(new BigDecimal("0.00"), userWallet.get().getBalanceBlocked());
        assertEquals(new BigDecimal("900.0000"), transaction.getBalanceAfterTransaction());
    }

    private UserStockPosition captureSavedPosition() {
        final ArgumentCaptor<UserStockPosition> newPositionCaptor = ArgumentCaptor.forClass(UserStockPosition.class);
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).saveUserStockPosition(newPositionCaptor.capture());

        return newPositionCaptor.getValue();
    }

}
