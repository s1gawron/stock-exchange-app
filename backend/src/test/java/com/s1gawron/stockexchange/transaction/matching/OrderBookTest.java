package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {

    private static final String TICKER = "AAPL";

    private static final LocalDateTime CREATE_DATE = LocalDateTime.of(2026, 11, 5, 15, 0);

    private static final Instant TRADE_TIME = Instant.parse("2026-11-05T15:30:00Z");

    private static final BigDecimal FULL_PARTICIPATION = BigDecimal.ONE;

    private OrderBook underTest;

    @BeforeEach
    void setUp() {
        underTest = new OrderBook(TICKER);
    }

    @Test
    void shouldFillPurchaseWhenTradePriceIsNotAboveLimitAtTradePrice() {
        underTest.add(purchase(1, "25.00", "10", 0));

        final List<Fill> result = underTest.match(List.of(trade("24.50", "100", 0)), FULL_PARTICIPATION);

        assertEquals(1, result.size());
        assertDecimal("10", result.get(0).quantity());
        assertDecimal("24.50", result.get(0).price());
        assertEquals(TRADE_TIME, result.get(0).lastTradeTimestamp());
        assertTrue(underTest.isEmpty());
    }

    @Test
    void shouldNotFillPurchaseWhenTradePriceIsAboveLimit() {
        underTest.add(purchase(1, "25.00", "10", 0));

        assertTrue(underTest.match(List.of(trade("25.01", "100", 0)), FULL_PARTICIPATION).isEmpty());
        assertTrue(underTest.contains(1));
    }

    @Test
    void shouldFillSellOnlyWhenTradePriceIsNotBelowLimit() {
        underTest.add(sell(1, "30.00", "5", 0));

        assertTrue(underTest.match(List.of(trade("29.99", "100", 0)), FULL_PARTICIPATION).isEmpty());

        final List<Fill> result = underTest.match(List.of(trade("30.00", "100", 1)), FULL_PARTICIPATION);
        assertDecimal("5", result.get(0).quantity());
        assertDecimal("30.00", result.get(0).price());
    }

    @Test
    void shouldPartiallyFillWhenTradeVolumeIsSmallerThanQuantityAndKeepRemainderInBook() {
        underTest.add(purchase(1, "25.00", "10", 0));

        final List<Fill> result = underTest.match(List.of(trade("25.00", "3", 0)), FULL_PARTICIPATION);

        assertDecimal("3", result.get(0).quantity());
        assertTrue(underTest.contains(1));

        final List<Fill> nextResult = underTest.match(List.of(trade("25.00", "100", 1)), FULL_PARTICIPATION);
        assertDecimal("7", nextResult.get(0).quantity());
        assertTrue(underTest.isEmpty());
    }

    @Test
    void shouldLimitPoolWithParticipationRate() {
        underTest.add(purchase(1, "25.00", "10", 0));

        final List<Fill> result = underTest.match(List.of(trade("25.00", "15", 0)), new BigDecimal("0.2"));

        assertDecimal("3", result.get(0).quantity());
    }

    @Test
    void shouldGiveVolumeToHighestPurchaseLimitFirstThenToOldest() {
        underTest.add(purchase(1, "25.00", "5", 0));
        underTest.add(purchase(2, "26.00", "5", 1));
        underTest.add(purchase(3, "25.00", "5", 2));

        final Map<Long, Fill> result = byTransactionId(underTest.match(List.of(trade("25.00", "8", 0)), FULL_PARTICIPATION));

        assertDecimal("5", result.get(2L).quantity());
        assertDecimal("3", result.get(1L).quantity());
        assertFalse(result.containsKey(3L));
    }

    @Test
    void shouldGiveVolumeToLowestSellLimitFirst() {
        underTest.add(sell(1, "31.00", "5", 0));
        underTest.add(sell(2, "30.00", "5", 1));

        final Map<Long, Fill> result = byTransactionId(underTest.match(List.of(trade("31.00", "6", 0)), FULL_PARTICIPATION));

        assertDecimal("5", result.get(2L).quantity());
        assertDecimal("1", result.get(1L).quantity());
    }

    @Test
    void shouldUseSeparateVolumePoolsForPurchasesAndSells() {
        underTest.add(purchase(1, "30.00", "10", 0));
        underTest.add(sell(2, "30.00", "10", 0));

        final Map<Long, Fill> result = byTransactionId(underTest.match(List.of(trade("30.00", "10", 0)), FULL_PARTICIPATION));

        assertDecimal("10", result.get(1L).quantity());
        assertDecimal("10", result.get(2L).quantity());
    }

    @Test
    void shouldMergeFillsOfOneTransactionIntoOneFillWithVolumeWeightedPrice() {
        underTest.add(purchase(1, "25.00", "10", 0));

        final List<Fill> result = underTest.match(List.of(trade("24.00", "4", 1), trade("25.00", "6", 2)), FULL_PARTICIPATION);

        assertEquals(1, result.size());
        assertDecimal("10", result.get(0).quantity());
        assertDecimal("24.60", result.get(0).price());
        assertEquals(TRADE_TIME.plusSeconds(2), result.get(0).lastTradeTimestamp());
    }

    @Test
    void shouldMatchTradesInTimeOrder() {
        underTest.add(purchase(1, "25.00", "5", 0));

        final List<Fill> result = underTest.match(List.of(trade("20.00", "5", 2), trade("25.00", "5", 1)), FULL_PARTICIPATION);

        assertDecimal("25.00", result.get(0).price());
    }

    @Test
    void shouldIgnoreTradesWithoutVolumeAndTradesOfOtherTickers() {
        underTest.add(purchase(1, "25.00", "5", 0));

        final List<Trade> trades = List.of(trade("25.00", "0", 0), new Trade("MSFT", new BigDecimal("25.00"), BigDecimal.TEN, TRADE_TIME));

        assertTrue(underTest.match(trades, FULL_PARTICIPATION).isEmpty());
    }

    @Test
    void shouldFillFractionalQuantities() {
        underTest.add(purchase(1, "25.00", "0.5", 0));

        final List<Fill> result = underTest.match(List.of(trade("25.00", "1", 0)), new BigDecimal("0.3"));

        assertDecimal("0.3", result.get(0).quantity());
    }

    @Test
    void shouldReplaceEntryWithSameTransactionIdAndRemoveEntries() {
        underTest.add(purchase(1, "25.00", "5", 0));
        underTest.add(purchase(1, "20.00", "5", 0));

        assertEquals(1, underTest.size());
        assertTrue(underTest.match(List.of(trade("21.00", "5", 0)), FULL_PARTICIPATION).isEmpty());

        underTest.remove(1);
        assertTrue(underTest.isEmpty());
    }

    @Test
    void shouldNotAcceptEntryOfOtherTicker() {
        final OrderBookEntry entry = new OrderBookEntry(1, 1, "MSFT", TransactionType.PURCHASE, BigDecimal.TEN, BigDecimal.ONE, CREATE_DATE);

        assertThrows(IllegalArgumentException.class, () -> underTest.add(entry));
    }

    private OrderBookEntry purchase(final long transactionId, final String limit, final String quantity, final int createSecondOffset) {
        return entry(transactionId, TransactionType.PURCHASE, limit, quantity, createSecondOffset);
    }

    private OrderBookEntry sell(final long transactionId, final String limit, final String quantity, final int createSecondOffset) {
        return entry(transactionId, TransactionType.SELL, limit, quantity, createSecondOffset);
    }

    private OrderBookEntry entry(final long transactionId, final TransactionType transactionType, final String limit, final String quantity,
        final int createSecondOffset) {
        return new OrderBookEntry(transactionId, 10 + transactionId, TICKER, transactionType, new BigDecimal(limit), new BigDecimal(quantity),
            CREATE_DATE.plusSeconds(createSecondOffset));
    }

    private Trade trade(final String price, final String volume, final int secondOffset) {
        return new Trade(TICKER, new BigDecimal(price), new BigDecimal(volume), TRADE_TIME.plusSeconds(secondOffset));
    }

    private Map<Long, Fill> byTransactionId(final List<Fill> fills) {
        return fills.stream().collect(Collectors.toMap(Fill::transactionId, Function.identity()));
    }

    private static void assertDecimal(final String expected, final BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "Expected: " + expected + " but was: " + actual);
    }
}
