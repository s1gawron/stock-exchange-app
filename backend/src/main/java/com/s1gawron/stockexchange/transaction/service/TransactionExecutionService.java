package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotFoundException;
import com.s1gawron.stockexchange.transaction.exception.WalletCurrencyMismatchException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.TransactionCreatorStrategy;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.TransactionProcessorStrategy;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionExecutionService {

    private final PurchaseTransactionCreator purchaseTransactionCreator;

    private final SellTransactionCreator sellTransactionCreator;

    private final PurchaseTransactionProcessor purchaseTransactionProcessor;

    private final SellTransactionProcessor sellTransactionProcessor;

    private final TransactionDAO transactionDAO;

    private final UserWalletService userWalletService;

    public TransactionExecutionService(final PurchaseTransactionCreator purchaseTransactionCreator, final SellTransactionCreator sellTransactionCreator,
        final PurchaseTransactionProcessor purchaseTransactionProcessor, final SellTransactionProcessor sellTransactionProcessor,
        final TransactionDAO transactionDAO, final UserWalletService userWalletService) {
        this.purchaseTransactionCreator = purchaseTransactionCreator;
        this.sellTransactionCreator = sellTransactionCreator;
        this.purchaseTransactionProcessor = purchaseTransactionProcessor;
        this.sellTransactionProcessor = sellTransactionProcessor;
        this.transactionDAO = transactionDAO;
        this.userWalletService = userWalletService;
    }

    @Transactional
    public void createAndProcessTransaction(final TransactionRequestDTO transactionRequestDTO, final BigDecimal currentPrice, final String stockCurrency) {
        final UserWallet userWallet = userWalletService.lockUserWallet();

        if (!userWallet.getCurrency().equals(stockCurrency)) {
            throw WalletCurrencyMismatchException.create(transactionRequestDTO.stockTicker(), stockCurrency, userWallet.getCurrency());
        }

        final long transactionId = createTransaction(transactionRequestDTO, stockCurrency);
        processTransaction(transactionId, currentPrice);
    }

    private long createTransaction(final TransactionRequestDTO transactionRequestDTO, final String stockCurrency) {
        final TransactionCreatorStrategy strategy = transactionRequestDTO.type().isPurchase() ? purchaseTransactionCreator : sellTransactionCreator;

        if (strategy.canCreateTransaction(transactionRequestDTO)) {
            return strategy.createTransaction(transactionRequestDTO, stockCurrency);
        }

        throw new IllegalStateException("Method should not execute here, because canCreateTransaction() throws exception if cannot be created!");
    }

    private void processTransaction(final long transactionId, final BigDecimal currentPrice) {
        final Transaction transaction = transactionDAO.getTransactionById(transactionId).orElseThrow(() -> TransactionNotFoundException.create(transactionId));
        final TransactionProcessorStrategy strategy = transaction.getTransactionType().isPurchase() ? purchaseTransactionProcessor : sellTransactionProcessor;

        if (strategy.canProcessTransaction(transaction, currentPrice)) {
            strategy.processTransaction(transaction, currentPrice);
        }
    }

}
