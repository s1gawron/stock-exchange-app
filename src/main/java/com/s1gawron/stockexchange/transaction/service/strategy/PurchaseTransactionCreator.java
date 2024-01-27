package com.s1gawron.stockexchange.transaction.service.strategy;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.exception.StockPurchasePriceLessThanZeroException;
import com.s1gawron.stockexchange.transaction.exception.TransactionInfoNotCollectedException;
import com.s1gawron.stockexchange.transaction.exception.WrongStockQuantityException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PurchaseTransactionCreator implements TransactionCreatorStrategy {

    private TransactionRequestDTO transactionRequestDTO;

    private final StockDataProvider stockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public PurchaseTransactionCreator(StockDataProvider stockDataProvider, UserWalletService userWalletService, TransactionDAO transactionDAO) {
        this.stockDataProvider = stockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override public void collectTransactionInfo(final TransactionRequestDTO transactionRequestDTO) {
        this.transactionRequestDTO = transactionRequestDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateTransaction() {
        if (transactionRequestDTO == null) {
            throw TransactionInfoNotCollectedException.create();
        }

        stockDataProvider.findStock(transactionRequestDTO.stockTicker());

        if (transactionRequestDTO.stockPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw StockPurchasePriceLessThanZeroException.create();
        }

        if (transactionRequestDTO.stockQuantity() <= 0) {
            throw WrongStockQuantityException.create();
        }

        final BigDecimal userWalletBalance = userWalletService.getUserWallet().getBalanceAvailable();
        final BigDecimal transactionCost = transactionRequestDTO.stockPrice().multiply(BigDecimal.valueOf(transactionRequestDTO.stockQuantity()));
        final BigDecimal maxAmountOfStockToPurchase = userWalletBalance.divide(transactionCost, 0, RoundingMode.DOWN);

        if (userWalletBalance.compareTo(transactionCost) < 0) {
            throw NotEnoughMoneyException.create(transactionCost, userWalletBalance, maxAmountOfStockToPurchase, transactionRequestDTO.stockTicker());
        }

        return true;
    }

    @Override
    @Transactional
    public void createTransaction() {
        final UserWallet userWallet = userWalletService.getUserWallet();
        final BigDecimal transactionCost = transactionRequestDTO.stockPrice().multiply(BigDecimal.valueOf(transactionRequestDTO.stockQuantity()));

        userWallet.blockFunds(transactionCost);
        userWalletService.updateUserWallet(userWallet);

        final Transaction transaction = Transaction.createFrom(userWallet.getOwnerId(), transactionRequestDTO);

        transactionDAO.saveTransaction(transaction);
    }

    @Override public TransactionRequestDTO getTransactionInfo() {
        return transactionRequestDTO;
    }
}
