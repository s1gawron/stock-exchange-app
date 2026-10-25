package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.validator.TransactionRequestDTOValidator;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotFoundException;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.TransactionCreatorStrategy;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.TransactionProcessorStrategy;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final PurchaseTransactionCreator purchaseTransactionCreator;

    private final SellTransactionCreator sellTransactionCreator;

    private final PurchaseTransactionProcessor purchaseTransactionProcessor;

    private final SellTransactionProcessor sellTransactionProcessor;

    private final TransactionDAO transactionDAO;

    private final UserWalletService userWalletService;

    private final StockDataProvider finnhubStockDataProvider;

    public TransactionService(final PurchaseTransactionCreator purchaseTransactionCreator, final SellTransactionCreator sellTransactionCreator,
        final PurchaseTransactionProcessor purchaseTransactionProcessor, final SellTransactionProcessor sellTransactionProcessor,
        final TransactionDAO transactionDAO, final UserWalletService userWalletService, final StockDataProvider finnhubStockDataProvider) {
        this.purchaseTransactionCreator = purchaseTransactionCreator;
        this.sellTransactionCreator = sellTransactionCreator;
        this.purchaseTransactionProcessor = purchaseTransactionProcessor;
        this.sellTransactionProcessor = sellTransactionProcessor;
        this.transactionDAO = transactionDAO;
        this.userWalletService = userWalletService;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
    }

    @Transactional
    public void validateCreateAndProcessTransaction(final TransactionRequestDTO transactionRequestDTO) {
        TransactionRequestDTOValidator.I.validate(transactionRequestDTO);
        userWalletService.lockUserWallet();

        final long transactionId = createTransaction(transactionRequestDTO);
        processTransaction(transactionId);
    }

    private long createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final TransactionCreatorStrategy strategy = transactionRequestDTO.type().isPurchase() ? purchaseTransactionCreator : sellTransactionCreator;

        if (strategy.canCreateTransaction(transactionRequestDTO)) {
            return strategy.createTransaction(transactionRequestDTO);
        }

        throw new IllegalStateException("Method should not execute here, because canCreateTransaction() throws exception if cannot be created!");
    }

    private void processTransaction(final long transactionId) {
        final Transaction transaction = transactionDAO.getTransactionById(transactionId).orElseThrow(() -> TransactionNotFoundException.create(transactionId));
        final TransactionProcessorStrategy strategy = transaction.getTransactionType().isPurchase() ? purchaseTransactionProcessor : sellTransactionProcessor;
        final BigDecimal currentPrice = getCurrentPrice(transaction.getTransactionPosition().getStockTicker());

        if (strategy.canProcessTransaction(transaction, currentPrice)) {
            strategy.processTransaction(transaction, currentPrice);
        }
    }

    private BigDecimal getCurrentPrice(final String stockTicker) {
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(stockTicker).stockQuote().currentPrice();

        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw TransactionProcessingException.createForUnavailablePrice(stockTicker);
        }

        return currentPrice;
    }

}