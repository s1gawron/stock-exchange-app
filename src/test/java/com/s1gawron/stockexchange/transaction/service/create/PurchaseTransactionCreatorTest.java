package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
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

    private StockDataProvider stockDataProviderMock;

    private UserWalletService userWalletServiceMock;

    private TransactionDAO transactionDAOMock;

    private PurchaseTransactionCreator underTest;

    @BeforeEach
    void setUp() {
        stockDataProviderMock = Mockito.mock(StockDataProvider.class);
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAOMock = Mockito.mock(TransactionDAO.class);
    }

    @Test
    void shouldValidateTransaction() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(stockDataProviderMock.findStock(transactionRequestDTO.stockTicker())).thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        final boolean result = underTest.validateTransaction();
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenStockIsNotFound() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "thisStockDoesNotExist",
            new BigDecimal("20.25"), 10);
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(stockDataProviderMock.getStockData(transactionRequestDTO.stockTicker()))
            .thenThrow(StockNotFoundException.createFromTicker(transactionRequestDTO.stockTicker()));

        assertThrows(StockNotFoundException.class, () -> underTest.validateTransaction());
    }

    @Test
    void shouldThrowExceptionWhenPurchasePriceIsLessThan0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("-10"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(stockDataProviderMock.findStock(transactionRequestDTO.stockTicker())).thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockPriceLteZeroException.class, () -> underTest.validateTransaction());
    }

    @Test
    void shouldThrowExceptionWhenPurchasePriceIs0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", BigDecimal.ZERO, 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(stockDataProviderMock.findStock(transactionRequestDTO.stockTicker())).thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockPriceLteZeroException.class, () -> underTest.validateTransaction());
    }

    @Test
    void shouldThrowExceptionWhenStockQuantityIsLessThan0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), -2);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(stockDataProviderMock.findStock(transactionRequestDTO.stockTicker())).thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockQuantityLteZeroException.class, () -> underTest.validateTransaction());
    }

    @Test
    void shouldThrowExceptionWhenStockQuantityIs0() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 0);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(stockDataProviderMock.findStock(transactionRequestDTO.stockTicker())).thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(StockQuantityLteZeroException.class, () -> underTest.validateTransaction());
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveEnoughMoneyToBuyStock() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("100.00"), new BigDecimal("100.00"));
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(stockDataProviderMock.findStock(transactionRequestDTO.stockTicker())).thenReturn(StockDataGeneratorHelper.I.getAppleSearchResponse());
        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(NotEnoughMoneyException.class, () -> underTest.validateTransaction());
    }

    @Test
    void shouldCreateTransaction() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        underTest = new PurchaseTransactionCreator(transactionRequestDTO, stockDataProviderMock, userWalletServiceMock, transactionDAOMock);

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        underTest.validateTransaction();
        underTest.createTransaction();

        assertEquals(new BigDecimal("12.50"), userWallet.getBalanceAvailable());
        assertEquals(new BigDecimal("202.50"), userWallet.getBalanceBlocked());
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).updateUserWallet(userWallet);
        Mockito.verify(transactionDAOMock, Mockito.times(1)).saveTransaction(Mockito.any(Transaction.class));
    }

}