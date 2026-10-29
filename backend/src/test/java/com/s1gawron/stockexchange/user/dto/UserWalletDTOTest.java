package com.s1gawron.stockexchange.user.dto;

import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.user.model.UserWallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserWalletDTOTest {

    @Test
    void shouldReturnZeroPercentageChangeWhenLastDayValueIsZero() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1L, new BigDecimal("100.00"), BigDecimal.ZERO);

        final UserWalletDTO result = UserWalletDTO.create(BigDecimal.ZERO, userWallet, Instant.now());

        assertEquals("USD", result.currency());
        assertEquals(new BigDecimal("100.00"), result.value());
        assertEquals(new BigDecimal("0.00"), result.valuePercentageChange());
    }

    @Test
    void shouldCalculatePercentageChange() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(1L, new BigDecimal("100.00"), new BigDecimal("80.00"));

        final UserWalletDTO result = UserWalletDTO.create(BigDecimal.ZERO, userWallet, Instant.now());

        assertEquals(new BigDecimal("25.00"), result.valuePercentageChange());
    }

}
