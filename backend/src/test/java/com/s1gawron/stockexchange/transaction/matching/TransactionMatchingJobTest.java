package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.transaction.matching.feed.TradeBuffer;
import com.s1gawron.stockexchange.transaction.matching.feed.TradeFeed;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import com.s1gawron.stockexchange.transaction.service.TransactionFillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionMatchingJobTest {

    private static final LocalDateTime CREATE_DATE = LocalDateTime.of(2026, 11, 5, 15, 0);

    private TradeBuffer tradeBuffer;

    private OrderBookRegistry orderBookRegistry;

    private OrderBookSynchronizer orderBookSynchronizerMock;

    private TransactionFillService transactionFillServiceMock;

    private TransactionMatchingJob underTest;

    @BeforeEach
    void setUp() {
        tradeBuffer = new TradeBuffer(100);
        orderBookRegistry = new OrderBookRegistry(Mockito.mock(TradeFeed.class), BigDecimal.ONE);
        orderBookSynchronizerMock = Mockito.mock(OrderBookSynchronizer.class);
        transactionFillServiceMock = Mockito.mock(TransactionFillService.class);
        underTest = new TransactionMatchingJob(tradeBuffer, orderBookRegistry, orderBookSynchronizerMock, transactionFillServiceMock);
    }

    @Test
    void shouldMatchBufferedTradesAndSaveFillsPerWallet() {
        orderBookRegistry.add(entry(1, 100, "AAPL"));
        orderBookRegistry.add(entry(2, 200, "AAPL"));
        orderBookRegistry.add(entry(3, 100, "MSFT"));
        tradeBuffer.publish(trade("AAPL"));
        tradeBuffer.publish(trade("MSFT"));

        underTest.matchTrades();

        final ArgumentCaptor<List<Fill>> firstWalletFills = fillsCaptor();
        Mockito.verify(transactionFillServiceMock).applyFills(Mockito.eq(100L), firstWalletFills.capture());
        assertEquals(List.of(1L, 3L), firstWalletFills.getValue().stream().map(Fill::transactionId).toList());

        final ArgumentCaptor<List<Fill>> secondWalletFills = fillsCaptor();
        Mockito.verify(transactionFillServiceMock).applyFills(Mockito.eq(200L), secondWalletFills.capture());
        assertEquals(List.of(2L), secondWalletFills.getValue().stream().map(Fill::transactionId).toList());

        assertTrue(tradeBuffer.drain().isEmpty());
        Mockito.verifyNoInteractions(orderBookSynchronizerMock);
    }

    @Test
    void shouldReloadTransactionsWhenFillsOfWalletCannotBeSaved() {
        orderBookRegistry.add(entry(1, 100, "AAPL"));
        tradeBuffer.publish(trade("AAPL"));
        Mockito.when(transactionFillServiceMock.applyFills(Mockito.eq(100L), Mockito.anyList())).thenThrow(new IllegalStateException("test"));

        underTest.matchTrades();

        Mockito.verify(orderBookSynchronizerMock).reload(List.of(1L));
    }

    @Test
    void shouldReloadTransactionsReturnedByFillService() {
        orderBookRegistry.add(entry(1, 100, "AAPL"));
        tradeBuffer.publish(trade("AAPL"));
        Mockito.when(transactionFillServiceMock.applyFills(Mockito.eq(100L), Mockito.anyList())).thenReturn(List.of(1L));

        underTest.matchTrades();

        Mockito.verify(orderBookSynchronizerMock).reload(List.of(1L));
    }

    @Test
    void shouldDoNothingWithoutTrades() {
        underTest.matchTrades();

        Mockito.verifyNoInteractions(transactionFillServiceMock, orderBookSynchronizerMock);
    }

    @Test
    void shouldDropTradesWhenBufferIsFull() {
        final TradeBuffer smallBuffer = new TradeBuffer(1);

        smallBuffer.publish(trade("AAPL"));
        smallBuffer.publish(trade("AAPL"));

        assertEquals(1, smallBuffer.drain().size());
        assertEquals(1, smallBuffer.getDroppedTrades());
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<Fill>> fillsCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    private OrderBookEntry entry(final long transactionId, final long walletId, final String ticker) {
        return new OrderBookEntry(transactionId, walletId, ticker, TransactionType.PURCHASE, new BigDecimal("25.00"), BigDecimal.ONE, CREATE_DATE);
    }

    private Trade trade(final String ticker) {
        return new Trade(ticker, new BigDecimal("25.00"), BigDecimal.TEN, Instant.parse("2026-11-05T15:30:00Z"));
    }
}
