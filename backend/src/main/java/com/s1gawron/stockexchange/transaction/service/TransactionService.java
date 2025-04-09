package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.validator.TransactionRequestDTOValidator;
import com.s1gawron.stockexchange.transaction.exception.TransactionNotFoundException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.service.create.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.create.TransactionCreatorStrategy;
import com.s1gawron.stockexchange.transaction.service.process.PurchaseTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.SellTransactionProcessor;
import com.s1gawron.stockexchange.transaction.service.process.TransactionProcessorStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void validateCreateAndProcessTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final long transactionId = validateAndCreateTransaction(transactionRequestDTO);
        processTransaction(transactionId);
    }

    private long validateAndCreateTransaction(final TransactionRequestDTO transactionRequestDTO) {
        TransactionRequestDTOValidator.I.validate(transactionRequestDTO);

        final TransactionCreatorStrategy strategy = transactionRequestDTO.type().isPurchase() ? purchaseTransactionCreator : sellTransactionCreator;

        if (strategy.canCreateTransaction(transactionRequestDTO)) {
            return strategy.createTransaction(transactionRequestDTO);
        }

        throw new IllegalStateException("Method should not execute here, because canCreateTransaction() throws exception if cannot be created!");
    }

    private void processTransaction(final long transactionId) {
        final Transaction transaction = transactionDAO.getTransactionById(transactionId).orElseThrow(() -> TransactionNotFoundException.create(transactionId));
        final TransactionProcessorStrategy strategy = transaction.getTransactionType().isPurchase() ? purchaseTransactionProcessor : sellTransactionProcessor;

        if (strategy.canProcessTransaction(transaction)) {
            strategy.processTransaction(transaction);
        }
    }

}