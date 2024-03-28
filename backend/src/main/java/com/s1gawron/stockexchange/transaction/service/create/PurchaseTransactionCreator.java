package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
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

    private final TransactionRequestDTO transactionRequestDTO;

    private final StockDataProvider stockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public PurchaseTransactionCreator(final TransactionRequestDTO transactionRequestDTO, final StockDataProvider stockDataProvider,
        final UserWalletService userWalletService, final TransactionDAO transactionDAO) {
        this.transactionRequestDTO = transactionRequestDTO;
        this.stockDataProvider = stockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateTransaction() {
        stockDataProvider.getStockData(transactionRequestDTO.stockTicker());

        if (transactionRequestDTO.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw StockPriceLteZeroException.create();
        }

        if (transactionRequestDTO.quantity() <= 0) {
            throw StockQuantityLteZeroException.create();
        }

        final BigDecimal userWalletBalance = userWalletService.getUserWallet().getBalanceAvailable();
        final BigDecimal transactionCost = transactionRequestDTO.price().multiply(transactionRequestDTO.quantityBD());
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
        final BigDecimal transactionCost = transactionRequestDTO.price().multiply(transactionRequestDTO.quantityBD());

        userWallet.blockBalance(transactionCost);
        userWalletService.updateUserWallet(userWallet);

        final Transaction transaction = Transaction.createFrom(userWallet.getWalletId(), transactionRequestDTO);
        transaction.setBalanceBlocked(transactionCost);
        transactionDAO.saveTransaction(transaction);
    }

}
