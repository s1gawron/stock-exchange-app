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
        final UserStock userStock = UserStockGeneratorHelper.I.getUserStock(
            UserStockGeneratorHelper.I.getAppleUserStockPosition(1L, new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("25.00"),
                UserStockGeneratorHelper.OPEN_DATE));

        final UserStockDTO result = UserStockDTO.create(userStock, StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00")));

        assertEquals(new BigDecimal("10.000000"), result.quantity());
        assertEquals(new BigDecimal("10.000000"), result.quantityBlocked());
        assertEquals(new BigDecimal("100.0000"), result.profitLoss());
    }

}
