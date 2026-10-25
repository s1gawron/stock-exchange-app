package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.usercontext.UserContextProvider;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.model.UserWalletSnapshot;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
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

        final List<UserStock> userStocks = userWalletDAO.getUserStocks(userWallet.getId());

        return new UserWalletSnapshot(userWallet, userStocks);
    }

    @Transactional(readOnly = true)
    public List<String> getUserStockTickers(final long walletId) {
        return userWalletDAO.getUserStocks(walletId).stream()
            .map(UserStock::getTicker)
            .toList();
    }

    @Transactional
    public void updateWalletAtTheEndOfTheDay(final Long walletId, final Map<String, BigDecimal> stockPrices) {
        final UserWallet userWallet = userWalletDAO.findByIdForUpdate(walletId)
            .orElseThrow(() -> UserWalletNotFoundException.create(walletId));

        final List<UserStock> userStocks = userWalletDAO.getUserStocks(walletId);
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
    public void lockUserWallet() {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();
        userWalletDAO.findUserWalletByUserIdForUpdate(userId)
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

        return userWalletDAO.getUserStock(userWallet.getId(), ticker);
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
            final BigDecimal stockQuantity = BigDecimal.valueOf(userStock.getQuantityAvailable() + userStock.getQuantityBlocked());
            final BigDecimal stockWalletValue = currentPrice.multiply(stockQuantity);

            stocksValue = stocksValue.add(stockWalletValue);
        }

        return stocksValue;
    }

    private BigDecimal getCurrentPrice(final String ticker) {
        return finnhubStockDataProvider.getStockData(ticker).stockQuote().currentPrice();
    }

}
