package com.s1gawron.stockexchange.user.dto;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.user.model.UserStock;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserStockDTOTest {

    @Test
    void shouldIncludeBlockedStockInProfitLoss() {
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(1L, 20, 10, new BigDecimal("25.00"));

        final UserStockDTO result = UserStockDTO.create(userStock, StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00")));

        assertEquals(10, result.quantity());
        assertEquals(10, result.quantityBlocked());
        assertEquals(new BigDecimal("100.00"), result.profitLoss());
    }

}
