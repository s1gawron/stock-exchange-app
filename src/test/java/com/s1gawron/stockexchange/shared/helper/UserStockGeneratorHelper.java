package com.s1gawron.stockexchange.shared.helper;

import com.s1gawron.stockexchange.user.model.UserStock;

import java.math.BigDecimal;
import java.util.List;

public enum UserStockGeneratorHelper {

    I;

    public UserStock getAppleUserStock(final long walletId) {
        return new UserStock(walletId, "AAPL", 100, new BigDecimal("25.00"));
    }

    public UserStock getAmazonUserStock(final long walletId) {
        return new UserStock(walletId, "AMZN", 150, new BigDecimal("32.00"));
    }

    public List<UserStock> getUserStocks(final long walletId) {
        return List.of(getAppleUserStock(walletId), getAmazonUserStock(walletId));
    }
}
