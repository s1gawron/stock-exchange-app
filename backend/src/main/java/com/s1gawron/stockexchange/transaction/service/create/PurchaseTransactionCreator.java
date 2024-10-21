package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PurchaseTransactionCreator implements TransactionCreatorStrategy {

    private final FinnhubStockDataProvider finnhubStockDataProvider;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public PurchaseTransactionCreator(final FinnhubStockDataProvider finnhubStockDataProvider, final UserWalletService userWalletService,
        final TransactionDAO transactionDAO) {
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public boolean canCreateTransaction(final TransactionRequestDTO transactionRequestDTO) {
        finnhubStockDataProvider.getStockData(transactionRequestDTO.stockTicker());

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
    public void createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final UserWallet userWallet = userWalletService.getUserWallet();
        final BigDecimal transactionCost = transactionRequestDTO.price().multiply(transactionRequestDTO.quantityBD());

        userWallet.blockBalance(transactionCost);
        userWalletService.updateUserWallet(userWallet);

        final Transaction transaction = Transaction.createFrom(userWallet.getWalletId(), transactionRequestDTO);
        transaction.setBalanceBlocked(transactionCost);
        transactionDAO.saveTransaction(transaction);
    }

}
