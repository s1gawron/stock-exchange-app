package com.s1gawron.stockexchange.transaction.service.strategy;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NoStockInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughStockException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SellTransactionCreator implements TransactionCreatorStrategy {

    private final TransactionRequestDTO transactionRequestDTO;

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public SellTransactionCreator(final TransactionRequestDTO transactionRequestDTO, final UserWalletService userWalletService,
        final TransactionDAO transactionDAO) {
        this.transactionRequestDTO = transactionRequestDTO;
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override public boolean validateTransaction() {
        if (transactionRequestDTO.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw StockPriceLteZeroException.create();
        }

        if (transactionRequestDTO.quantity() <= 0) {
            throw StockQuantityLteZeroException.create();
        }

        final Optional<UserStock> userStock = userWalletService.getUserStock(transactionRequestDTO.stockTicker());

        if (userStock.isEmpty()) {
            throw NoStockInUserWalletException.create(transactionRequestDTO.stockTicker());
        }

        if (userStock.get().getQuantityAvailable() < transactionRequestDTO.quantity()) {
            throw NotEnoughStockException.create(transactionRequestDTO.stockTicker(), userStock.get().getQuantityAvailable(), transactionRequestDTO.quantity());
        }

        return true;
    }

    @Override public void createTransaction() {
        final UserStock userStock = userWalletService.getUserStock(transactionRequestDTO.stockTicker())
            .orElseThrow(() -> NoStockInUserWalletException.create(transactionRequestDTO.stockTicker()));

        userStock.blockStock(transactionRequestDTO.quantity());
        userWalletService.updateUserStock(userStock);

        final Transaction transaction = Transaction.createFrom(userStock.getWalletId(), transactionRequestDTO);
        transactionDAO.saveTransaction(transaction);
    }

}
