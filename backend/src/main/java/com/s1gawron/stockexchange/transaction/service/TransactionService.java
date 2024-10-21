package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotFoundException;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.TransactionCreatorStrategy;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.TransactionProcessorStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final PurchaseTransactionCreator purchaseTransactionCreator;

    private final SellTransactionCreator sellTransactionCreator;

    private final PurchaseTransactionProcessor purchaseTransactionProcessor;

    private final SellTransactionProcessor sellTransactionProcessor;

    private final TransactionDAO transactionDAO;

    public TransactionService(final PurchaseTransactionCreator purchaseTransactionCreator, final SellTransactionCreator sellTransactionCreator,
        final PurchaseTransactionProcessor purchaseTransactionProcessor, final SellTransactionProcessor sellTransactionProcessor,
        final TransactionDAO transactionDAO) {
        this.purchaseTransactionCreator = purchaseTransactionCreator;
        this.sellTransactionCreator = sellTransactionCreator;
        this.purchaseTransactionProcessor = purchaseTransactionProcessor;
        this.sellTransactionProcessor = sellTransactionProcessor;
        this.transactionDAO = transactionDAO;
    }

    @Transactional
    public void createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final TransactionCreatorStrategy strategy = transactionRequestDTO.type().isPurchase() ? purchaseTransactionCreator : sellTransactionCreator;

        if (strategy.canCreateTransaction(transactionRequestDTO)) {
            strategy.createTransaction(transactionRequestDTO);
        }
    }

    @Transactional(readOnly = true)
    public List<Long> getNewTransactionIds() {
        return transactionDAO.getNewTransactionIds();
    }

    @Transactional
    public void changeTransactionsStatus(final List<Long> transactionIds, final TransactionStatus newTransactionStatus) {
        transactionDAO.changeTransactionsStatus(transactionIds, newTransactionStatus);
    }

    @Transactional
    public void processTransaction(final long transactionId) {
        final Transaction transaction = transactionDAO.getTransactionById(transactionId).orElseThrow(() -> TransactionNotFoundException.create(transactionId));
        final TransactionProcessorStrategy strategy = transaction.getTransactionType().isPurchase() ? purchaseTransactionProcessor : sellTransactionProcessor;

        if (strategy.cannotProcessTransaction(transaction)) {
            throw TransactionProcessingException.create();
        }

        strategy.processTransaction(transaction);
    }
}