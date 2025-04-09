package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NoStockInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughStockException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SellTransactionCreator implements TransactionCreatorStrategy {

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    public SellTransactionCreator(final UserWalletService userWalletService, final TransactionDAO transactionDAO) {
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public boolean canCreateTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final Optional<UserStock> userStock = userWalletService.getUserStock(transactionRequestDTO.stockTicker());

        if (userStock.isEmpty()) {
            throw NoStockInUserWalletException.create(transactionRequestDTO.stockTicker());
        }

        if (userStock.get().getQuantityAvailable() < transactionRequestDTO.quantity()) {
            throw NotEnoughStockException.create(transactionRequestDTO.stockTicker(), userStock.get().getQuantityAvailable(), transactionRequestDTO.quantity());
        }

        return true;
    }

    @Override
    public Long createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final UserStock userStock = userWalletService.getUserStock(transactionRequestDTO.stockTicker())
            .orElseThrow(() -> NoStockInUserWalletException.create(transactionRequestDTO.stockTicker()));

        userStock.blockStock(transactionRequestDTO.quantity());
        userWalletService.updateUserStock(userStock);

        final Transaction transaction = Transaction.createFrom(userStock.getWalletId(), transactionRequestDTO);
        transactionDAO.saveTransaction(transaction);

        return transaction.getId();
    }

}
