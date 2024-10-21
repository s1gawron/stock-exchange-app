package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionCreatorTest {

    private FinnhubStockDataProvider finnhubStockDataProviderMock;

    private UserWalletService userWalletServiceMock;

    private TransactionDAO transactionDAOMock;

    private PurchaseTransactionCreator underTest;

    @BeforeEach
    void setUp() {
        finnhubStockDataProviderMock = Mockito.mock(FinnhubStockDataProvider.class);
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAOMock = Mockito.mock(TransactionDAO.class);
        underTest = new PurchaseTransactionCreator(finnhubStockDataProviderMock, userWalletServiceMock, transactionDAOMock);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeCreated() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(finnhubStockDataProviderMock.findStock(transactionRequestDTO.stockTicker()))
            .thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        final boolean result = underTest.canCreateTransaction(transactionRequestDTO);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenStockIsNotFound() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "thisStockDoesNotExist",
            new BigDecimal("20.25"), 10);

        Mockito.when(finnhubStockDataProviderMock.getStockData(transactionRequestDTO.stockTicker()))
            .thenThrow(StockNotFoundException.createFromTicker(transactionRequestDTO.stockTicker()));

        assertThrows(StockNotFoundException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenPurchasePriceIsLessThan0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("-10"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(finnhubStockDataProviderMock.findStock(transactionRequestDTO.stockTicker()))
            .thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockPriceLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenPurchasePriceIs0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", BigDecimal.ZERO, 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(finnhubStockDataProviderMock.findStock(transactionRequestDTO.stockTicker()))
            .thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockPriceLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockQuantityIsLessThan0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), -2);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(finnhubStockDataProviderMock.findStock(transactionRequestDTO.stockTicker()))
            .thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockQuantityLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockQuantityIs0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 0);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(finnhubStockDataProviderMock.findStock(transactionRequestDTO.stockTicker()))
            .thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockQuantityLteZeroException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveEnoughMoneyToBuyStock() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("100.00"), new BigDecimal("100.00"));

        Mockito.when(finnhubStockDataProviderMock.findStock(transactionRequestDTO.stockTicker()))
            .thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(NotEnoughMoneyException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldCreateTransaction() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        underTest.createTransaction(transactionRequestDTO);

        assertEquals(new BigDecimal("12.50"), userWallet.getBalanceAvailable());
        assertEquals(new BigDecimal("202.50"), userWallet.getBalanceBlocked());
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).updateUserWallet(userWallet);
        Mockito.verify(transactionDAOMock, Mockito.times(1)).saveTransaction(Mockito.any(Transaction.class));
    }

}