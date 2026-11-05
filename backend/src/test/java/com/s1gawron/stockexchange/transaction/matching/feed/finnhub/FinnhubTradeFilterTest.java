package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinnhubTradeFilterTest {

    private final FinnhubTradeFilter underTest = new FinnhubTradeFilter(Set.of("1", "7", "8"), false);

    @Test
    void shouldAcceptTradeWithOnlyNormalConditions() {
        assertEquals(FinnhubTradeFilter.Result.ACCEPTED, underTest.check(trade("100", List.of("1", "7", "8"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12", "24", "25", "18", "19", "35", "36", "21", "30", "3", "5", "17", "32", "26", "37", "28", "39"})
    void shouldRejectTradeWithSpecialCondition(final String condition) {
        assertEquals(FinnhubTradeFilter.Result.SPECIAL_CONDITION, underTest.check(trade("100", List.of("1", condition))));
    }

    @Test
    void shouldRejectTradeWithoutVolume() {
        assertEquals(FinnhubTradeFilter.Result.NO_VOLUME, underTest.check(trade("0", List.of("1"))));
        assertEquals(FinnhubTradeFilter.Result.NO_VOLUME, underTest.check(trade(null, List.of("1"))));
    }

    @Test
    void shouldRejectTradeWithoutConditionsByDefault() {
        assertEquals(FinnhubTradeFilter.Result.NO_CONDITIONS, underTest.check(trade("100", null)));
        assertEquals(FinnhubTradeFilter.Result.NO_CONDITIONS, underTest.check(trade("100", List.of())));
    }

    @Test
    void shouldAcceptTradeWithoutConditionsWhenConfigured() {
        final FinnhubTradeFilter filter = new FinnhubTradeFilter(Set.of("1"), true);

        assertEquals(FinnhubTradeFilter.Result.ACCEPTED, filter.check(trade("100", null)));
    }

    @Test
    void shouldRejectInvalidTrade() {
        assertEquals(FinnhubTradeFilter.Result.INVALID, underTest.check(new FinnhubTradeDTO(null, BigDecimal.TEN, 1, BigDecimal.TEN, List.of("1"))));
        assertEquals(FinnhubTradeFilter.Result.INVALID, underTest.check(new FinnhubTradeDTO("AAPL", BigDecimal.ZERO, 1, BigDecimal.TEN, List.of("1"))));
        assertEquals(FinnhubTradeFilter.Result.INVALID, underTest.check(new FinnhubTradeDTO("AAPL", BigDecimal.TEN, 0, BigDecimal.TEN, List.of("1"))));
    }

    private FinnhubTradeDTO trade(final String volume, final List<String> conditions) {
        return new FinnhubTradeDTO("AAPL", new BigDecimal("187.42"), 1762354800000L, volume == null ? null : new BigDecimal(volume), conditions);
    }
}
