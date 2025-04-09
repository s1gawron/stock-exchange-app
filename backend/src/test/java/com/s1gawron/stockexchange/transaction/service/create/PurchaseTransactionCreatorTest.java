package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.InMemoryStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.transaction.dao.impl.InMemoryTransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseTransactionCreatorTest {

    private InMemoryStockDataProvider stockDataProvider;

    private UserWalletService userWalletServiceMock;

    private InMemoryTransactionDAO transactionDAO;

    private PurchaseTransactionCreator underTest;

    @BeforeEach
    void setUp() {
        stockDataProvider = new InMemoryStockDataProvider();
        userWalletServiceMock = Mockito.mock(UserWalletService.class);
        transactionDAO = new InMemoryTransactionDAO();
        underTest = new PurchaseTransactionCreator(stockDataProvider, userWalletServiceMock, transactionDAO);
    }

    @Test
    void shouldReturnTrueWhenTransactionCanBeCreated() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("22.00"));
        stockDataProvider.addStockData(appleStock);

        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        final boolean result = underTest.canCreateTransaction(transactionRequestDTO);
        assertTrue(result);
    }

    @Test
    void shouldThrowExceptionWhenStockIsNotFound() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "thisStockDoesNotExist",
            new BigDecimal("20.25"), 10);

        assertThrows(StockNotFoundException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveEnoughMoneyToBuyStock() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("20.00"));
        stockDataProvider.addStockData(appleStock);

        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("100.00"), new BigDecimal("100.00"));

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        assertThrows(NotEnoughMoneyException.class, () -> underTest.canCreateTransaction(transactionRequestDTO));
    }

    @Test
    void shouldCreateTransaction() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("20.25"), 10);
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1, new BigDecimal("215.00"), new BigDecimal("215.00"));
        ReflectionTestUtils.setField(userWallet, "id", 1L);

        Mockito.when(userWalletServiceMock.getUserWallet()).thenReturn(userWallet);

        underTest.createTransaction(transactionRequestDTO);

        assertEquals(new BigDecimal("12.50"), userWallet.getBalanceAvailable());
        assertEquals(new BigDecimal("202.50"), userWallet.getBalanceBlocked());
        assertTrue(transactionDAO.getTransactionById(0L).isPresent());
        Mockito.verify(userWalletServiceMock, Mockito.times(1)).updateUserWallet(userWallet);
    }

}