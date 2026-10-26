package com.s1gawron.stockexchange.transaction.service.create;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.NotEnoughMoneyException;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class PurchaseTransactionCreator implements TransactionCreatorStrategy {

    private final UserWalletService userWalletService;

    private final TransactionDAO transactionDAO;

    private final Clock clock;

    public PurchaseTransactionCreator(final UserWalletService userWalletService, final TransactionDAO transactionDAO, final Clock clock) {
        this.userWalletService = userWalletService;
        this.transactionDAO = transactionDAO;
        this.clock = clock;
    }

    @Override
    public boolean canCreateTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final BigDecimal userWalletBalance = userWalletService.getUserWallet().getBalanceAvailable();
        final BigDecimal transactionCost = MoneyRounding.I.money(transactionRequestDTO.price().multiply(transactionRequestDTO.quantityBD()));

        if (userWalletBalance.compareTo(transactionCost) < 0) {
            final BigDecimal maxAmountOfStockToPurchase = userWalletBalance.divide(transactionRequestDTO.price(), 0, RoundingMode.DOWN);
            throw NotEnoughMoneyException.create(transactionCost, userWalletBalance, maxAmountOfStockToPurchase, transactionRequestDTO.stockTicker());
        }

        return true;
    }

    @Override
    public Long createTransaction(final TransactionRequestDTO transactionRequestDTO) {
        final UserWallet userWallet = userWalletService.getUserWallet();
        final BigDecimal transactionCost = MoneyRounding.I.money(transactionRequestDTO.price().multiply(transactionRequestDTO.quantityBD()));

        userWallet.blockBalance(transactionCost);
        userWalletService.updateUserWallet(userWallet);

        final Transaction transaction = Transaction.createFrom(userWallet.getId(), transactionRequestDTO, LocalDateTime.now(clock));
        transaction.setBalanceBlocked(transactionCost);
        transactionDAO.saveTransaction(transaction);

        return transaction.getId();
    }

}
