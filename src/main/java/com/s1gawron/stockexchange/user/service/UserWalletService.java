package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.usercontext.UserContextProvider;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserWalletDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserWalletService {

    private static final BigDecimal ONE_HUNDRED_PERCENT = new BigDecimal("100");

    private final UserWalletDAO userWalletDAO;

    private final StockDataProvider stockDataProvider;

    private final Clock clock;

    public UserWalletService(final UserWalletDAO userWalletDAO, final StockDataProvider stockDataProvider, final Clock clock) {
        this.userWalletDAO = userWalletDAO;
        this.stockDataProvider = stockDataProvider;
        this.clock = clock;
    }

    @Transactional
    public UserWalletDTO updateAndGetUserWallet() {
        final Long userId = UserContextProvider.I.getLoggedInUser().getUserId();
        return updateUserWalletImpl(userId).toUserWalletDTO();
    }

    @Transactional
    public void updateUserWalletAtTheEndOfTheDay(final Long userId) {
        final UserWallet userWallet = updateUserWalletImpl(userId);

        userWallet.setPreviousWalletValue(userWallet.getWalletValue());
        userWallet.setWalletPercentageChange(BigDecimal.ZERO);
        userWallet.setLastUpdateDate(LocalDateTime.now(clock));
    }

    private UserWallet updateUserWalletImpl(final long userId) {
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.create(userId));

        final AtomicReference<BigDecimal> stockValue = new AtomicReference<>(BigDecimal.ZERO);

        userWallet.getUserStocks().forEach(userStock -> {
            final String ticker = userStock.getTicker();
            final BigDecimal stockPrice = stockDataProvider.getStockData(ticker).stockQuote().currentPrice();
            final BigDecimal stockQuantityBigDecimal = BigDecimal.valueOf(userStock.getQuantity());
            final BigDecimal walletValueOfSpecificStock = stockPrice.multiply(stockQuantityBigDecimal);

            stockValue.set(stockValue.get().add(walletValueOfSpecificStock));
        });

        userWallet.setStockValue(stockValue.get());
        userWallet.setLastUpdateDate(LocalDateTime.now(clock));

        final BigDecimal totalWalletValue = userWallet.getStockValue().add(userWallet.getBalanceAvailable());
        userWallet.setWalletValue(totalWalletValue);

        final BigDecimal differenceBetweenCurrentWalletValueAndPreviousWalletValue = userWallet.getWalletValue().subtract(userWallet.getPreviousWalletValue());
        final BigDecimal walletPercentageChange = differenceBetweenCurrentWalletValueAndPreviousWalletValue
            .divide(userWallet.getPreviousWalletValue(), 4, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED_PERCENT)
            .setScale(2, RoundingMode.HALF_UP);

        userWallet.setWalletPercentageChange(walletPercentageChange);

        return userWallet;
    }

}
