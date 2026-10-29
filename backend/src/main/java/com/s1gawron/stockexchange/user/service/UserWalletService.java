package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.shared.usercontext.UserContextProvider;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.model.UserWalletSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserWalletService {

    private static final Logger log = LoggerFactory.getLogger(UserWalletService.class);

    private final UserWalletDAO userWalletDAO;

    private final StockDataProvider finnhubStockDataProvider;

    private final Clock clock;

    public UserWalletService(final UserWalletDAO userWalletDAO, final StockDataProvider finnhubStockDataProvider, final Clock clock) {
        this.userWalletDAO = userWalletDAO;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<UserWallet> getUserWallet(final long walletId) {
        return userWalletDAO.findById(walletId);
    }

    @Transactional(readOnly = true)
    public UserWalletSnapshot getUserWalletSnapshot() {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));

        final List<UserStock> userStocks = UserStock.fromPositions(userWalletDAO.getUserStockPositions(userWallet.getId()));

        return new UserWalletSnapshot(userWallet, userStocks);
    }

    @Transactional(readOnly = true)
    public List<String> getUserStockTickers(final long walletId) {
        return userWalletDAO.getUserStockPositions(walletId).stream()
            .map(UserStockPosition::getTicker)
            .distinct()
            .toList();
    }

    @Transactional
    public void updateWalletAtTheEndOfTheDay(final Long walletId, final Map<String, BigDecimal> stockPrices) {
        final UserWallet userWallet = userWalletDAO.findByIdForUpdate(walletId)
            .orElseThrow(() -> UserWalletNotFoundException.create(walletId));

        final List<UserStock> userStocks = UserStock.fromPositions(userWalletDAO.getUserStockPositions(walletId));
        final BigDecimal stockValue = getStockValue(userStocks, stockPrices);
        final BigDecimal walletValue = stockValue.add(userWallet.getBalanceAvailable()).add(userWallet.getBalanceBlocked());

        userWallet.setLastDayValue(walletValue);
        userWallet.setLastDayUpdateDate(LocalDateTime.now(clock));

        userWalletDAO.updateUserWallet(userWallet);

        log.info("Wallet#{} end of day update finished at: {}", walletId, LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public UserWallet getUserWallet() {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();
        return userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public UserWallet lockUserWallet() {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();
        return userWalletDAO.findUserWalletByUserIdForUpdate(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));
    }

    @Transactional
    public void updateUserWallet(final UserWallet userWallet) {
        userWalletDAO.updateUserWallet(userWallet);
    }

    @Transactional(readOnly = true)
    public Optional<UserStock> getUserStock(final String ticker) {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(userId)
            .orElseThrow(() -> UserWalletNotFoundException.createForUser(userId));

        return getUserStock(userWallet.getId(), ticker);
    }

    @Transactional(readOnly = true)
    public Optional<UserStock> getUserStock(final long walletId, final String ticker) {
        return UserStock.fromTickerPositions(userWalletDAO.getUserStockPositions(walletId, ticker));
    }

    @Transactional
    public void updateUserStockPosition(final UserStockPosition userStockPosition) {
        userWalletDAO.updateUserStockPosition(userStockPosition);
    }

    @Transactional
    public void saveUserStockPosition(final UserStockPosition userStockPosition) {
        userWalletDAO.saveUserStockPosition(userStockPosition);
    }

    @Transactional
    public void deleteUserStockPosition(final UserStockPosition userStockPosition) {
        userWalletDAO.deleteUserStockPosition(userStockPosition);
    }

    @Transactional(readOnly = true)
    public List<Long> getAllWalletIds() {
        return userWalletDAO.getAllWalletIds();
    }

    public Map<String, BigDecimal> getStockPrices(final List<String> tickers) {
        return tickers.stream()
            .distinct()
            .collect(Collectors.toMap(Function.identity(), this::getCurrentPrice));
    }

    public BigDecimal getStockValue(final List<UserStock> userStocks, final Map<String, BigDecimal> stockPrices) {
        BigDecimal stocksValue = BigDecimal.ZERO;

        if (userStocks.isEmpty()) {
            return stocksValue;
        }

        for (final UserStock userStock : userStocks) {
            final BigDecimal currentPrice = Optional.ofNullable(stockPrices.get(userStock.getTicker()))
                .orElseGet(() -> getCurrentPrice(userStock.getTicker()));
            final BigDecimal stockWalletValue = currentPrice.multiply(userStock.getAllStockQuantity());

            stocksValue = stocksValue.add(stockWalletValue);
        }

        return MoneyRounding.I.money(stocksValue);
    }

    private BigDecimal getCurrentPrice(final String ticker) {
        return finnhubStockDataProvider.getStockData(ticker).stockQuote().currentPrice();
    }

}
