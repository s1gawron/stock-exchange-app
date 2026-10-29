package com.s1gawron.stockexchange.shared.helper;

import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public enum UserStockGeneratorHelper {

    I;

    public static final LocalDateTime OPEN_DATE = LocalDateTime.of(2026, 1, 5, 10, 0);

    public UserStockPosition getAppleUserStockPosition(final long walletId) {
        return getAppleUserStockPosition(walletId, new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("25.00"), OPEN_DATE);
    }

    public UserStockPosition getAppleUserStockPosition(final long walletId, final BigDecimal quantityAvailable, final BigDecimal quantityToBlock,
        final BigDecimal openPrice, final LocalDateTime openDate) {
        final UserStockPosition position = new UserStockPosition(walletId, "AAPL", "USD", quantityAvailable.add(quantityToBlock), openPrice, openDate);

        if (quantityToBlock.signum() > 0) {
            position.blockStock(quantityToBlock);
        }

        return position;
    }

    public UserStockPosition getAmazonUserStockPosition(final long walletId) {
        return new UserStockPosition(walletId, "AMZN", "USD", new BigDecimal("150"), new BigDecimal("32.00"), OPEN_DATE);
    }

    public List<UserStockPosition> getUserStockPositions(final long walletId) {
        return List.of(getAppleUserStockPosition(walletId), getAmazonUserStockPosition(walletId));
    }

    public UserStock getAppleUserStock(final long walletId) {
        return getUserStock(getAppleUserStockPosition(walletId));
    }

    public UserStock getUserStock(final UserStockPosition... positions) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i].getId() == null) {
                ReflectionTestUtils.setField(positions[i], "id", (long) i + 1);
            }
        }

        return UserStock.fromTickerPositions(List.of(positions)).orElseThrow();
    }
}
