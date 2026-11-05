package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.transaction.matching.feed.TradeFeed;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookRegistryTest {

    private static final LocalDateTime CREATE_DATE = LocalDateTime.of(2026, 11, 5, 15, 0);

    private TradeFeed tradeFeedMock;

    private OrderBookRegistry underTest;

    @BeforeEach
    void setUp() {
        tradeFeedMock = Mockito.mock(TradeFeed.class);
        underTest = new OrderBookRegistry(tradeFeedMock, BigDecimal.ONE);
    }

    @Test
    void shouldSubscribeTickerOnceWhenFirstTransactionIsAdded() {
        underTest.add(entry(1, "AAPL", "5"));
        underTest.add(entry(2, "AAPL", "5"));

        Mockito.verify(tradeFeedMock, Mockito.times(1)).subscribe("AAPL");
        assertEquals(Set.of("AAPL"), underTest.getTickers());
    }

    @Test
    void shouldUnsubscribeTickerWhenLastTransactionIsRemoved() {
        underTest.add(entry(1, "AAPL", "5"));
        underTest.add(entry(2, "AAPL", "5"));

        underTest.remove(1);
        Mockito.verify(tradeFeedMock, Mockito.never()).unsubscribe("AAPL");

        underTest.remove(2);
        Mockito.verify(tradeFeedMock).unsubscribe("AAPL");
        assertTrue(underTest.getTickers().isEmpty());
    }

    @Test
    void shouldRemoveFilledTransactionsAndUnsubscribeAfterMatching() {
        underTest.add(entry(1, "AAPL", "5"));

        final List<Fill> result = underTest.match("AAPL", List.of(new Trade("AAPL", new BigDecimal("25.00"), BigDecimal.TEN, Instant.now())));

        assertEquals(1, result.size());
        assertFalse(underTest.contains(1));
        Mockito.verify(tradeFeedMock).unsubscribe("AAPL");
    }

    @Test
    void shouldReturnNoFillsForTickerWithoutOrderBook() {
        assertTrue(underTest.match("MSFT", List.of(new Trade("MSFT", BigDecimal.TEN, BigDecimal.TEN, Instant.now()))).isEmpty());
    }

    @Test
    void shouldNotAddEntryWithoutRemainingQuantity() {
        underTest.add(entry(1, "AAPL", "0"));

        assertFalse(underTest.contains(1));
        Mockito.verifyNoInteractions(tradeFeedMock);
    }

    @Test
    void shouldNotAcceptParticipationRateOutsideZeroAndOne() {
        assertThrows(IllegalArgumentException.class, () -> new OrderBookRegistry(tradeFeedMock, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new OrderBookRegistry(tradeFeedMock, new BigDecimal("1.01")));
    }

    private OrderBookEntry entry(final long transactionId, final String ticker, final String quantity) {
        return new OrderBookEntry(transactionId, 1, ticker, TransactionType.PURCHASE, new BigDecimal("25.00"), new BigDecimal(quantity), CREATE_DATE);
    }
}
