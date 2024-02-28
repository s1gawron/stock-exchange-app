package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotFoundException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.TransactionCreatorStrategy;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.TransactionProcessorStrategy;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final ObjectProvider<PurchaseTransactionCreator> purchaseTransactionCreator;

    private final ObjectProvider<SellTransactionCreator> sellTransactionCreator;

    private final ObjectProvider<PurchaseTransactionProcessor> purchaseTransactionProcessor;

    private final StockDataProvider stockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public TransactionService(final ObjectProvider<PurchaseTransactionCreator> purchaseTransactionCreator,
        final ObjectProvider<SellTransactionCreator> sellTransactionCreator, final ObjectProvider<PurchaseTransactionProcessor> purchaseTransactionProcessor,
        final StockDataProvider stockDataProvider, final UserWalletService userWalletService, final TransactionDAO transactionDAO) {
        this.purchaseTransactionCreator = purchaseTransactionCreator;
        this.sellTransactionCreator = sellTransactionCreator;
        this.purchaseTransactionProcessor = purchaseTransactionProcessor;
        this.stockDataProvider = stockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    public void createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final TransactionCreatorStrategy strategy = getCreatorStrategy(transactionRequestDTO);

        if (strategy.canCreateTransaction()) {
            strategy.createTransaction();
        }
    }

    private TransactionCreatorStrategy getCreatorStrategy(final TransactionRequestDTO transactionRequestDTO) {
        if (transactionRequestDTO.type().isPurchase()) {
            return purchaseTransactionCreator.getObject(transactionRequestDTO, stockDataProvider, userWalletService, transactionDAO);
        }

        return sellTransactionCreator.getObject(transactionRequestDTO, userWalletService, transactionDAO);
    }

    @Transactional(readOnly = true)
    public List<Long> getNewTransactionIds() {
        return transactionDAO.getNewTransactionIds();
    }

    @Transactional
    public void changeTransactionsStatus(final List<Long> transactionIds, final TransactionStatus newTransactionStatus) {
        transactionDAO.changeTransactionsStatus(transactionIds, newTransactionStatus);
    }

    @Transactional(readOnly = true)
    public void processTransaction(final long transactionId) {
        final Transaction transaction = transactionDAO.getTransactionById(transactionId).orElseThrow(() -> TransactionNotFoundException.create(transactionId));
        final TransactionProcessorStrategy strategy = getProcessorStrategy(transaction);

        if (strategy.canProcessTransaction()) {
            strategy.processTransaction();
        }
    }

    private TransactionProcessorStrategy getProcessorStrategy(final Transaction transaction) {
        if (transaction.getTransactionType().isPurchase()) {
            purchaseTransactionProcessor.getObject(transaction, stockDataProvider, userWalletService, transactionDAO);
        }

        return null;
    }
}