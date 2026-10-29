package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.transaction.dao.impl.InMemoryTransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NoStockInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NoStockPositionInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughStockException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SellTransactionCreatorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private UserWalletService userWalletServiceMock;

    private InMemoryTransactionDAO transactionDAO;

    private SellTransactionCreator underTest;

    @BeforeEach
    void setUp() {
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAO = new InMemoryTransactionDAO();
        underTest = new SellTransactionCreator(userWalletServiceMock, transactionDAO, CLOCK);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeCreated() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"),
            new BigDecimal("10"));

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        final boolean result = underTest.canCreateTransaction(transactionRequestDTO);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveStockToSell() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"),
            new BigDecimal("10"));

        assertThrows(NoStockInUserWalletException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveEnoughStockToSell() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"),
            new BigDecimal("100.000001"));

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(NotEnoughStockException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenSelectedPositionDoesNotExist() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"),
            new BigDecimal("10"), 99L);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(NoStockPositionInUserWalletException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenSelectedPositionDoesNotHaveEnoughStock() {
        final UserStockPosition olderPosition = getApplePosition("5", 1);
        final UserStockPosition newerPosition = getApplePosition("20", 2);
        final UserStock userStock = UserStockGeneratorHelper.I.getUserStock(olderPosition, newerPosition);
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"),
            new BigDecimal("10"), olderPosition.getId());

        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(Optional.of(userStock));

        assertThrows(NotEnoughStockException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldCreateTransactionAndBlockStockFromOldestPositionsFirst() {
        final UserStockPosition olderPosition = getApplePosition("5", 1);
        final UserStockPosition newerPosition = getApplePosition("20", 2);
        final UserStock userStock = UserStockGeneratorHelper.I.getUserStock(newerPosition, olderPosition);
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"),
            new BigDecimal("7.5"));

        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(Optional.of(userStock));

        underTest.createTransaction(transactionRequestDTO, "USD");

        assertEquals(new BigDecimal("0.000000"), olderPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("5.000000"), olderPosition.getQuantityBlocked());
        assertEquals(new BigDecimal("17.500000"), newerPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("2.500000"), newerPosition.getQuantityBlocked());
        Mockito.verify(userWalletServiceMock).updateUserStockPosition(olderPosition);
        Mockito.verify(userWalletServiceMock).updateUserStockPosition(newerPosition);

        final Transaction transaction = transactionDAO.getTransactionById(0L).orElseThrow();
        assertEquals(1L, transaction.getWalletId());
        assertEquals("USD", transaction.getTransactionPosition().getStockCurrency());
    }

    @Test
    void shouldCreateTransactionAndBlockStockOnSelectedPosition() {
        final UserStockPosition olderPosition = getApplePosition("5", 1);
        final UserStockPosition newerPosition = getApplePosition("20", 2);
        final UserStock userStock = UserStockGeneratorHelper.I.getUserStock(olderPosition, newerPosition);
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"),
            new BigDecimal("7.5"), newerPosition.getId());

        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(Optional.of(userStock));

        underTest.createTransaction(transactionRequestDTO, "USD");

        assertEquals(new BigDecimal("5.000000"), olderPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("0.000000"), olderPosition.getQuantityBlocked());
        assertEquals(new BigDecimal("12.500000"), newerPosition.getQuantityAvailable());
        assertEquals(new BigDecimal("7.500000"), newerPosition.getQuantityBlocked());
        Mockito.verify(userWalletServiceMock, Mockito.never()).updateUserStockPosition(olderPosition);
        Mockito.verify(userWalletServiceMock).updateUserStockPosition(newerPosition);
        assertTrue(transactionDAO.getTransactionById(0L).isPresent());
    }

    private UserStockPosition getApplePosition(final String quantity, final int openDayOffset) {
        return UserStockGeneratorHelper.I.getAppleUserStockPosition(1, new BigDecimal(quantity), BigDecimal.ZERO, new BigDecimal("25.00"),
            UserStockGeneratorHelper.OPEN_DATE.plusDays(openDayOffset));
    }

}
