package pl.eizodev.app.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.eizodev.app.stock.dataprovider.StockDataProvider;
import pl.eizodev.app.user.dto.UserWalletDTO;
import pl.eizodev.app.user.exception.UserWalletNotFoundException;
import pl.eizodev.app.user.model.UserStock;
import pl.eizodev.app.user.model.UserWallet;
import pl.eizodev.app.user.repository.UserWalletRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserWalletService {

    private final UserWalletRepository userWalletRepository;

    private final StockDataProvider stockDataProvider;

    @Transactional
    public UserWalletDTO updateAndGetUserWallet(final String username) {
        final UserWallet userWallet = userWalletRepository.findByUser_Username(username)
            .orElseThrow(() -> UserWalletNotFoundException.create(username));

        final List<UserStock> userStockList = userWallet.getUserStock();

        if (userStockList.isEmpty()) {
            userWallet.setStockValue(BigDecimal.ZERO);
        } else {
            BigDecimal stockValue = new BigDecimal(0);

            for (UserStock userStock : userStockList) {
                final String ticker = userStock.getTicker();
                final BigDecimal stockPrice = stockDataProvider.getStockData(ticker).getStockQuote().getCurrentPrice();
                final BigDecimal stockQuantityBigDecimal = BigDecimal.valueOf(userStock.getQuantity());

                stockValue = stockValue.add(stockPrice.multiply(stockQuantityBigDecimal));
            }

            userWallet.setStockValue(stockValue);
        }

        final int currentDayOfYear = LocalDateTime.now().getDayOfYear();
        final int lastUpdateDayOfYear = userWallet.getUpdateDate().getDayOfYear();
        final int currentYear = LocalDateTime.now().getYear();
        final int lastUpdateYear = userWallet.getUpdateDate().getYear();
        final boolean currentDayIsSomeNextDayAfterLastUpdateDate = (currentYear > lastUpdateYear) && (currentDayOfYear > lastUpdateDayOfYear);

        //daily update of previous wallet value
        if (currentDayIsSomeNextDayAfterLastUpdateDate) {
            final BigDecimal walletValue = userWallet.getWalletValue();
            userWallet.setPreviousWalletValue(walletValue);
        }

        userWallet.setUpdateDate(LocalDateTime.now());

        final BigDecimal totalWalletValue = userWallet.getStockValue().add(userWallet.getBalanceAvailable());
        userWallet.setWalletValue(totalWalletValue);

        final BigDecimal differenceBetweenCurrentWalletValueAndPreviousWalletValue = userWallet.getWalletValue().subtract(userWallet.getPreviousWalletValue());
        final BigDecimal hundredPercent = new BigDecimal(100);

        userWallet.setWalletPercentageChange(differenceBetweenCurrentWalletValueAndPreviousWalletValue
            .divide(userWallet.getPreviousWalletValue(), RoundingMode.HALF_DOWN)
            .multiply(hundredPercent));

        return userWallet.toUserWalletDTO();
    }

}
