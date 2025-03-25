package com.s1gawron.stockexchange.shared.helper;

import com.s1gawron.stockexchange.user.model.UserWallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public enum UserWalletGeneratorHelper {

    I;

    public UserWallet getUserWallet(final long ownerId, final BigDecimal balanceAvailable, final BigDecimal previousWalletValue) {
        return getUserWallet(ownerId, balanceAvailable, previousWalletValue, BigDecimal.ZERO);
    }

    public UserWallet getUserWallet(final long ownerId, final BigDecimal balanceAvailable, final BigDecimal previousWalletValue,
        final BigDecimal transactionCost) {
        final UserWallet userWallet = UserWallet.createUserWallet(ownerId, balanceAvailable);

        userWallet.setLastDayValue(previousWalletValue);
        userWallet.setLastDayUpdateDate(LocalDateTime.of(2022, 4, 11, 17, 56, 22));
        userWallet.blockBalance(transactionCost);

        return userWallet;
    }

}
