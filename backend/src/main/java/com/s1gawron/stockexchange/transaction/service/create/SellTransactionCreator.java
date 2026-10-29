package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NoStockInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NoStockPositionInUserWalletException;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughStockException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SellTransactionCreator implements TransactionCreatorStrategy {

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    private final Clock clock;

    public SellTransactionCreator(final UserWalletService userWalletService, final TransactionDAO transactionDAO, final Clock clock) {
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
        this.clock = clock;
    }

    @Override
    public boolean canCreateTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final String stockTicker = transactionRequestDTO.stockTicker();
        final UserStock userStock = userWalletService.getUserStock(stockTicker)
            .orElseThrow(() -> NoStockInUserWalletException.create(stockTicker));

        if (transactionRequestDTO.positionId() != null) {
            final UserStockPosition position = getPosition(userStock, transactionRequestDTO.positionId());

            if (position.getQuantityAvailable().compareTo(transactionRequestDTO.quantity()) < 0) {
                throw NotEnoughStockException.createForPosition(stockTicker, transactionRequestDTO.positionId(), position.getQuantityAvailable(),
                    transactionRequestDTO.quantity());
            }

            return true;
        }

        if (userStock.getQuantityAvailable().compareTo(transactionRequestDTO.quantity()) < 0) {
            throw NotEnoughStockException.create(stockTicker, userStock.getQuantityAvailable(), transactionRequestDTO.quantity());
        }

        return true;
    }

    @Override
    public Long createTransaction(final TransactionRequestDTO transactionRequestDTO, final String stockCurrency) {
        final UserStock userStock = userWalletService.getUserStock(transactionRequestDTO.stockTicker())
            .orElseThrow(() -> NoStockInUserWalletException.create(transactionRequestDTO.stockTicker()));

        for (final UserStockPosition position : blockStock(userStock, transactionRequestDTO)) {
            userWalletService.updateUserStockPosition(position);
        }

        final long walletId = userStock.getPositions().get(0).getWalletId();
        final Transaction transaction = Transaction.createFrom(walletId, transactionRequestDTO, stockCurrency, LocalDateTime.now(clock));
        transactionDAO.saveTransaction(transaction);

        return transaction.getId();
    }

    private List<UserStockPosition> blockStock(final UserStock userStock, final TransactionRequestDTO transactionRequestDTO) {
        if (transactionRequestDTO.positionId() == null) {
            return userStock.blockStock(transactionRequestDTO.quantity());
        }

        final UserStockPosition position = getPosition(userStock, transactionRequestDTO.positionId());
        position.blockStock(transactionRequestDTO.quantity());

        return List.of(position);
    }

    private UserStockPosition getPosition(final UserStock userStock, final long positionId) {
        return userStock.getPosition(positionId)
            .orElseThrow(() -> NoStockPositionInUserWalletException.create(userStock.getTicker(), positionId));
    }

}
