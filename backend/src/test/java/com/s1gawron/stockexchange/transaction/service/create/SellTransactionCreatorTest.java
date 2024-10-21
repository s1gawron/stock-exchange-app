package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NoStockInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughStockException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SellTransactionCreatorTest {

    private UserWalletService userWalletServiceMock;

    private TransactionDAO transactionDAOMock;

    private SellTransactionCreator underTest;

    @BeforeEach
    void setUp() {
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAOMock = Mockito.mock(TransactionDAO.class);
        underTest = new SellTransactionCreator(userWalletServiceMock, transactionDAOMock);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeCreated() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 10);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        final boolean result = underTest.canCreateTransaction(transactionRequestDTO);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenSellPriceIsLessThan0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("-10.00"), 10);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(StockPriceLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenSellPriceIs0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", BigDecimal.ZERO, 10);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(StockPriceLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockQuantityIsLessThan0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), -10);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(StockQuantityLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockQuantityIs0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 0);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(StockQuantityLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveStockToSell() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 10);

        assertThrows(NoStockInUserWalletException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveEnoughStockToSell() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 9999);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        assertThrows(NotEnoughStockException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldCreateTransaction() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("25.00"), 10);

        final Optional<UserStock> userStock = Optional.of(UserStockGeneratorHelper.I.getAppleUserStock(1));
        Mockito.when(userWalletServiceMock.getUserStock(transactionRequestDTO.stockTicker())).thenReturn(userStock);

        underTest.createTransaction(transactionRequestDTO);

        assertEquals(userStock.get().getQuantityAvailable(), 90);
        assertEquals(userStock.get().getQuantityBlocked(), 10);
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).updateUserStock(userStock.get());
        Mockito.verify(transactionDAOMock, Mockito.times(1)).saveTransaction(Mockito.any(Transaction.class));
    }

}