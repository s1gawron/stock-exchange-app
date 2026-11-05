package com.s1gawron.stockexchange.transaction.matching.feed;

import com.s1gawron.stockexchange.transaction.matching.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded buffer between the trade feed and the matching job. When it is full, new trades are dropped, so open transactions get less volume.
 */
@Component
public class TradeBuffer {

    private static final Logger log = LoggerFactory.getLogger(TradeBuffer.class);

    private final BlockingQueue<Trade> trades;

    private final AtomicLong droppedTrades = new AtomicLong();

    public TradeBuffer(@Value("${transaction.matching.buffer-capacity:100000}") final int capacity) {
        this.trades = new ArrayBlockingQueue<>(capacity);
    }

    public void publish(final Trade trade) {
        if (!trades.offer(trade)) {
            final long dropped = droppedTrades.incrementAndGet();

            if (dropped % 1000 == 1) {
                log.warn("Trade buffer is full, {} trades were dropped so far", dropped);
            }
        }
    }

    public List<Trade> drain() {
        final List<Trade> drained = new ArrayList<>();
        trades.drainTo(drained);

        return drained;
    }

    public long getDroppedTrades() {
        return droppedTrades.get();
    }
}
