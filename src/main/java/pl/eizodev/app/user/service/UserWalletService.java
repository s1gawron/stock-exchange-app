package pl.eizodev.app.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.eizodev.app.stock.dataprovider.StockDataProvider;
import pl.eizodev.app.user.dto.UserWalletDTO;
import pl.eizodev.app.user.exception.UserWalletNotFoundException;
import pl.eizodev.app.user.model.UserWallet;
import pl.eizodev.app.user.repository.UserWalletRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@AllArgsConstructor
public class UserWalletService {

    private static final BigDecimal ONE_HUNDRED_PERCENT = new BigDecimal("100");

    private final UserWalletRepository userWalletRepository;

    private final StockDataProvider stockDataProvider;

    private final Clock clock;

    @Transactional
    public UserWalletDTO updateAndGetUserWallet(final String username) {
        return updateUserWalletImpl(username).toUserWalletDTO();
    }

    @Transactional
    public void updateUserWalletsAtTheEndOfTheDay(final List<String> usernames) {
        usernames.forEach(username -> {
            final UserWallet userWallet = updateUserWalletImpl(username);

            userWallet.setPreviousWalletValue(userWallet.getWalletValue());
            userWallet.setWalletPercentageChange(BigDecimal.ZERO);
            userWallet.setLastUpdateDate(LocalDateTime.now(clock));
        });
    }

    private UserWallet updateUserWalletImpl(final String username) {
        final UserWallet userWallet = userWalletRepository.findByUser_Username(username)
            .orElseThrow(() -> UserWalletNotFoundException.create(username));

        final AtomicReference<BigDecimal> stockValue = new AtomicReference<>(BigDecimal.ZERO);

        userWallet.getUserStocks().forEach(userStock -> {
            final String ticker = userStock.getTicker();
            final BigDecimal stockPrice = stockDataProvider.getStockData(ticker).getStockQuote().getCurrentPrice();
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
