package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.transaction.service.strategy.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.strategy.TransactionCreatorStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final ObjectProvider<PurchaseTransactionCreator> purchaseTransactionCreator;

    public TransactionService(final ObjectProvider<PurchaseTransactionCreator> purchaseTransactionCreator) {
        this.purchaseTransactionCreator = purchaseTransactionCreator;
    }

    public void createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final TransactionCreatorStrategy strategy = getStrategy(transactionRequestDTO.transactionType());

        strategy.collectTransactionInfo(transactionRequestDTO);
        strategy.validateTransaction();
        strategy.createTransaction();
    }

    private TransactionCreatorStrategy getStrategy(final TransactionType transactionType) {
        if (transactionType.isPurchase()) {
            return purchaseTransactionCreator.getObject();
        }

        return null;
    }
}