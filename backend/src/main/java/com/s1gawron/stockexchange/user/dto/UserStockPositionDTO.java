package com.s1gawron.stockexchange.user.dto;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.user.model.UserStockPosition;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserStockPositionDTO(Long id,
                                   BigDecimal quantity,
                                   BigDecimal quantityBlocked,
                                   BigDecimal openPrice,
                                   LocalDateTime openDate,
                                   BigDecimal profitLoss) {

    public static UserStockPositionDTO create(final UserStockPosition userStockPosition, final BigDecimal currentPrice) {
        final BigDecimal singleStockProfitLoss = currentPrice.subtract(userStockPosition.getOpenPrice());
        final BigDecimal allStockProfitLoss = MoneyRounding.I.money(singleStockProfitLoss.multiply(userStockPosition.getAllStockQuantity()));

        return new UserStockPositionDTO(userStockPosition.getId(),
            userStockPosition.getQuantityAvailable(),
            userStockPosition.getQuantityBlocked(),
            userStockPosition.getOpenPrice(),
            userStockPosition.getOpenDate(),
            allStockProfitLoss);
    }
}
