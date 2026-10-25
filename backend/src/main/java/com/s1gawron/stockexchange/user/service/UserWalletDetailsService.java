package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.user.dto.UserStockDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWalletSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserWalletDetailsService {

    private final UserWalletService userWalletService;

    private final StockDataProvider finnhubStockDataProvider;

    private final Clock clock;

    public UserWalletDetailsService(final UserWalletService userWalletService, final StockDataProvider finnhubStockDataProvider, final Clock clock) {
        this.userWalletService = userWalletService;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.clock = clock;
    }

    public UserWalletDTO getUserWalletDTO() {
        final UserWalletSnapshot userWalletSnapshot = userWalletService.getUserWalletSnapshot();
        final List<String> tickers = userWalletSnapshot.userStocks().stream()
            .map(UserStock::getTicker)
            .toList();
        final Map<String, BigDecimal> stockPrices = userWalletService.getStockPrices(tickers);
        final BigDecimal stockValue = userWalletService.getStockValue(userWalletSnapshot.userStocks(), stockPrices);

        return UserWalletDTO.create(stockValue, userWalletSnapshot.userWallet(), LocalDateTime.now(clock));
    }

    public List<UserStockDTO> getUserStocks() {
        final UserWalletSnapshot userWalletSnapshot = userWalletService.getUserWalletSnapshot();

        return userWalletSnapshot.userStocks().stream()
            .map(stock -> UserStockDTO.create(stock, finnhubStockDataProvider.getStockData(stock.getTicker())))
            .toList();
    }

}
