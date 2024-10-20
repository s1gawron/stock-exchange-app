package com.s1gawron.stockexchange.shared.helper;

import com.s1gawron.stockexchange.user.model.UserStock;

import java.math.BigDecimal;
import java.util.List;

public enum UserStockGeneratorHelper {

    I;

    public UserStock getAppleUserStock(final long walletId) {
        return getAppleUserStock(walletId, 100, new BigDecimal("25.00"));
    }

    public UserStock getAppleUserStock(final long walletId, final int quantityAvailable, final BigDecimal averagePurchasePrice) {
        return new UserStock(walletId, "AAPL", quantityAvailable, averagePurchasePrice);
    }

    public UserStock getAppleUserStock(final long walletId, final int quantityAvailable, final int quantityToBlock, final BigDecimal averagePurchasePrice) {
        final UserStock aapl = new UserStock(walletId, "AAPL", quantityAvailable, averagePurchasePrice);
        aapl.blockStock(quantityToBlock);

        return aapl;
    }

    public UserStock getAppleUserStock(final long walletId, final int blockedQuantity) {
        return getAppleUserStock(walletId, 100, blockedQuantity);
    }

    public UserStock getAppleUserStock(final long walletId, final int quantityAvailable, final int blockedQuantity) {
        final UserStock userStock = new UserStock(walletId, "AAPL", quantityAvailable, new BigDecimal("25.00"));

        userStock.blockStock(blockedQuantity);

        return userStock;
    }

    public UserStock getAmazonUserStock(final long walletId) {
        return new UserStock(walletId, "AMZN", 150, new BigDecimal("32.00"));
    }

    public List<UserStock> getUserStocks(final long walletId) {
        return List.of(getAppleUserStock(walletId), getAmazonUserStock(walletId));
    }
}
