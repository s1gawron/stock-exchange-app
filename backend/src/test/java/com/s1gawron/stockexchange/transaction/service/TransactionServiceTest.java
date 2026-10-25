package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.TransactionCreatorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

class TransactionServiceTest {

    private static final long TRANSACTION_ID = 1L;

    private PurchaseTransactionCreator purchaseTransactionCreatorMock;

    private PurchaseTransactionProcessor purchaseTransactionProcessorMock;

    private TransactionDAO transactionDAOMock;

    private StockDataProvider stockDataProviderMock;

    private TransactionService underTest;

    @BeforeEach
    void setUp() {
        purchaseTransactionCreatorMock = Mockito.mock(PurchaseTransactionCreator.class);
        purchaseTransactionProcessorMock = Mockito.mock(PurchaseTransactionProcessor.class);
        transactionDAOMock = Mockito.mock(TransactionDAO.class);
        stockDataProviderMock = Mockito.mock(StockDataProvider.class);

        underTest = new TransactionService(purchaseTransactionCreatorMock, Mockito.mock(SellTransactionCreator.class), purchaseTransactionProcessorMock,
            Mockito.mock(SellTransactionProcessor.class), transactionDAOMock, Mockito.mock(UserWalletService.class), stockDataProviderMock);
    }

    @Test
    void shouldUseTheSameStockPriceForCheckAndProcessing() {
        final BigDecimal currentPrice = new BigDecimal("45.00");
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("60.00"), 10);
        final Transaction transaction = TransactionCreatorHelper.I.createAppleStockTransactionWithBalanceBlocked(TransactionType.PURCHASE);

        Mockito.when(purchaseTransactionCreatorMock.canCreateTransaction(transactionRequestDTO)).thenReturn(true);
        Mockito.when(purchaseTransactionCreatorMock.createTransaction(transactionRequestDTO)).thenReturn(TRANSACTION_ID);
        Mockito.when(transactionDAOMock.getTransactionById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));
        Mockito.when(stockDataProviderMock.getStockData(Mockito.anyString())).thenReturn(StockDataGeneratorHelper.I.getAppleStock(currentPrice));
        Mockito.when(purchaseTransactionProcessorMock.canProcessTransaction(transaction, currentPrice)).thenReturn(true);

        underTest.validateCreateAndProcessTransaction(transactionRequestDTO);

        Mockito.verify(stockDataProviderMock, Mockito.times(1)).getStockData(Mockito.anyString());
        Mockito.verify(purchaseTransactionProcessorMock).canProcessTransaction(transaction, currentPrice);
        Mockito.verify(purchaseTransactionProcessorMock).processTransaction(transaction, currentPrice);
    }

}
