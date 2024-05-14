package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.usercontext.UserContextProvider;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.dto.UserStockDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserWalletService {

    private final UserWalletDAO userWalletDAO;

    private final FinnhubStockDataProvider finnhubStockDataProvider;

    private final Clock clock;

    public UserWalletService(final UserWalletDAO userWalletDAO, final FinnhubStockDataProvider finnhubStockDataProvider, final Clock clock) {
        this.userWalletDAO = userWalletDAO;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<UserWallet> getUserWallet(final long walletId) {
        return userWalletDAO.findById(walletId);
    }

    @Transactional
    public UserWalletDTO updateAndGetUserWalletDTO() {
        final long userId = UserContextProvider.I.getLoggedInUser().getUserId();
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));

        final List<UserStock> userStocks = userWalletDAO.getUserStocks(userWallet.getWalletId());
        final BigDecimal stockValue = getStockValue(userStocks);

        userWallet.setLastUpdateDate(LocalDateTime.now(clock));
        userWalletDAO.updateUserWallet(userWallet);

        return UserWalletDTO.create(stockValue, userWallet);
    }

    @Transactional
    public void updateUserWalletAtTheEndOfTheDay(final Long userId) {
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));

        final List<UserStock> userStocks = userWalletDAO.getUserStocks(userWallet.getWalletId());
        final BigDecimal stockValue = getStockValue(userStocks);
        final BigDecimal walletValue = stockValue.add(userWallet.getBalanceAvailable()).add(userWallet.getBalanceBlocked());

        userWallet.setLastDayValue(walletValue);
        userWallet.setLastUpdateDate(LocalDateTime.now(clock));

        userWalletDAO.updateUserWallet(userWallet);
    }

    @Transactional(readOnly = true)
    public List<UserStockDTO> getUserStocks() {
        final long userId = UserContextProvider.I.getLoggedInUser().getUserId();
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));

        final List<UserStock> userStocks = userWalletDAO.getUserStocks(userWallet.getWalletId());

        return userStocks.stream()
            .map(stock -> {
                final StockDataDTO stockData = finnhubStockDataProvider.getStockData(stock.getTicker());
                return UserStockDTO.create(stock, stockData);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public UserWallet getUserWallet() {
        final long userId = UserContextProvider.I.getLoggedInUser().getUserId();
        return userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));
    }

    @Transactional
    public void updateUserWallet(final UserWallet userWallet) {
        userWalletDAO.updateUserWallet(userWallet);
    }

    @Transactional(readOnly = true)
    public Optional<UserStock> getUserStock(final String ticker) {
        final long userId = UserContextProvider.I.getLoggedInUser().getUserId();
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));

        return userWalletDAO.getUserStock(userWallet.getWalletId(), ticker);
    }

    @Transactional(readOnly = true)
    public Optional<UserStock> getUserStock(final long walletId, final String ticker) {
        return userWalletDAO.getUserStock(walletId, ticker);
    }

    @Transactional
    public void updateUserStock(final UserStock userStock) {
        userWalletDAO.updateUserStock(userStock);
    }

    @Transactional
    public void saveUserStock(final UserStock userStock) {
        userWalletDAO.saveUserStock(userStock);
    }

    @Transactional
    public void deleteUserStock(final UserStock userStock) {
        userWalletDAO.deleteUserStock(userStock);
    }

    private BigDecimal getStockValue(final List<UserStock> userStocks) {
        BigDecimal stocksValue = BigDecimal.ZERO;

        if (userStocks.isEmpty()) {
            return stocksValue;
        }

        for (final UserStock userStock : userStocks) {
            final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(userStock.getTicker()).stockQuote().currentPrice();
            final BigDecimal stockQuantity = BigDecimal.valueOf(userStock.getQuantityAvailable());
            final BigDecimal stockWalletValue = currentPrice.multiply(stockQuantity);

            stocksValue = stocksValue.add(stockWalletValue);
        }

        return stocksValue;
    }
}
