package com.s1gawron.stockexchange.user.dto;

import com.s1gawron.stockexchange.user.model.UserWallet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record UserWalletDTO(BigDecimal stockValue,
                            BigDecimal balanceAvailable,
                            BigDecimal balanceBlocked,
                            BigDecimal value,
                            BigDecimal lastDayValue,
                            BigDecimal valuePercentageChange,
                            LocalDateTime lastUpdateDate) {

    private static final BigDecimal ONE_HUNDRED_PERCENT = new BigDecimal("100");

    public static UserWalletDTO create(final BigDecimal stockValue, final UserWallet userWallet, final LocalDateTime updateDate) {
        final BigDecimal walletValue = stockValue.add(userWallet.getBalanceAvailable()).add(userWallet.getBalanceBlocked());

        final BigDecimal differenceBetweenCurrentValueAndPreviousValue = walletValue.subtract(userWallet.getLastDayValue());
        final BigDecimal walletValuePercentageChange = differenceBetweenCurrentValueAndPreviousValue
            .divide(userWallet.getLastDayValue(), 4, RoundingMode.HALF_UP)
            .multiply(ONE_HUNDRED_PERCENT)
            .setScale(2, RoundingMode.HALF_UP);

        return new UserWalletDTO(stockValue, userWallet.getBalanceAvailable(), userWallet.getBalanceBlocked(), walletValue, userWallet.getLastDayValue(),
            walletValuePercentageChange, updateDate);
    }

}
