package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.service.strategy.PurchaseTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.strategy.SellTransactionCreator;
import com.s1gawron.stockexchange.transaction.service.strategy.TransactionCreatorStrategy;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final ObjectProvider<PurchaseTransactionCreator> purchaseTransactionCreator;

    private final ObjectProvider<SellTransactionCreator> sellTransactionCreator;

    private final StockDataProvider stockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public TransactionService(final ObjectProvider<PurchaseTransactionCreator> purchaseTransactionCreator,
        final ObjectProvider<SellTransactionCreator> sellTransactionCreator, final StockDataProvider stockDataProvider,
        final UserWalletService userWalletService, final TransactionDAO transactionDAO) {
        this.purchaseTransactionCreator = purchaseTransactionCreator;
        this.sellTransactionCreator = sellTransactionCreator;
        this.stockDataProvider = stockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    public void createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final TransactionCreatorStrategy strategy = getStrategy(transactionRequestDTO);
        strategy.validateTransaction();
        strategy.createTransaction();
    }

    private TransactionCreatorStrategy getStrategy(final TransactionRequestDTO transactionRequestDTO) {
        if (transactionRequestDTO.type().isPurchase()) {
            return purchaseTransactionCreator.getObject(transactionRequestDTO, stockDataProvider, userWalletService, transactionDAO);
        }

        return sellTransactionCreator.getObject(transactionRequestDTO, userWalletService, transactionDAO);
    }
}