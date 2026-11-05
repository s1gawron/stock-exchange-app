package com.s1gawron.stockexchange.user.model;

import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserStockTest {

    @Test
    void shouldGroupPositionsByTickerAndSortThemFromOldest() {
        final UserStockPosition newerApplePosition = getApplePosition("1", "30.00", 2);
        final UserStockPosition olderApplePosition = getApplePosition("1", "20.00", 1);
        final UserStockPosition amazonPosition = UserStockGeneratorHelper.I.getAmazonUserStockPosition(1);

        final List<UserStock> result = UserStock.fromPositions(List.of(newerApplePosition, amazonPosition, olderApplePosition));

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals(List.of(olderApplePosition, newerApplePosition), result.get(0).getPositions());
        assertEquals("AMZN", result.get(1).getTicker());
    }

    @Test
    void shouldComputeQuantitiesAndWeightedAveragePurchasePrice() {
        final UserStockPosition firstPosition = getApplePosition("0.5", "20.00", 1);
        final UserStockPosition secondPosition = getApplePosition("1.5", "40.00", 2);
        secondPosition.blockStock(new BigDecimal("0.25"));

        final UserStock result = UserStockGeneratorHelper.I.getUserStock(firstPosition, secondPosition);

        assertEquals(new BigDecimal("1.750000"), result.getQuantityAvailable());
        assertEquals(new BigDecimal("0.250000"), result.getQuantityBlocked());
        assertEquals(new BigDecimal("2.000000"), result.getAllStockQuantity());
        assertEquals(new BigDecimal("35.0000"), result.getAveragePurchasePrice());
        assertEquals("USD", result.getCurrency());
    }

    @Test
    void shouldThrowExceptionWhenBlockingMoreStockThanAvailable() {
        final UserStock userStock = UserStockGeneratorHelper.I.getUserStock(getApplePosition("1", "20.00", 1), getApplePosition("1", "20.00", 2));

        assertThrows(IllegalStateException.class, () -> userStock.blockStock(new BigDecimal("2.000001")));
    }

    @Test
    void shouldBlockStockOnOldestPositionsFirstAndReturnBlockedQuantityPerPosition() {
        final UserStockPosition olderPosition = getApplePosition("1", "20.00", 1);
        final UserStockPosition newerPosition = getApplePosition("2", "30.00", 2);
        final UserStock userStock = UserStockGeneratorHelper.I.getUserStock(newerPosition, olderPosition);

        final List<UserStockBlock> result = userStock.blockStock(new BigDecimal("1.5"));

        assertEquals(2, result.size());
        assertEquals(olderPosition, result.get(0).position());
        assertEquals(0, new BigDecimal("1").compareTo(result.get(0).quantity()));
        assertEquals(newerPosition, result.get(1).position());
        assertEquals(0, new BigDecimal("0.5").compareTo(result.get(1).quantity()));
    }

    @Test
    void shouldThrowExceptionWhenReleasingMoreStockThanBlocked() {
        final UserStockPosition position = getApplePosition("1", "20.00", 1);
        position.blockStock(new BigDecimal("0.5"));

        assertThrows(IllegalStateException.class, () -> position.releaseStock(new BigDecimal("0.6")));
    }

    @Test
    void shouldMoveBlockedStockBackToAvailableWhenUnblocking() {
        final UserStockPosition position = getApplePosition("1", "20.00", 1);
        position.blockStock(new BigDecimal("0.5"));

        position.unblockStock(new BigDecimal("0.2"));

        assertEquals(0, new BigDecimal("0.7").compareTo(position.getQuantityAvailable()));
        assertEquals(0, new BigDecimal("0.3").compareTo(position.getQuantityBlocked()));
    }

    @Test
    void shouldAddStockToPositionAndRecalculateWeightedOpenPrice() {
        final UserStockPosition position = getApplePosition("1", "20.00", 1);

        position.addStock(new BigDecimal("3"), new BigDecimal("40.00"));

        assertEquals(new BigDecimal("4.000000"), position.getQuantityAvailable());
        assertEquals(new BigDecimal("35.0000"), position.getOpenPrice());
    }

    private UserStockPosition getApplePosition(final String quantity, final String openPrice, final int openDayOffset) {
        return UserStockGeneratorHelper.I.getAppleUserStockPosition(1, new BigDecimal(quantity), BigDecimal.ZERO, new BigDecimal(openPrice),
            UserStockGeneratorHelper.OPEN_DATE.plusDays(openDayOffset));
    }

}
