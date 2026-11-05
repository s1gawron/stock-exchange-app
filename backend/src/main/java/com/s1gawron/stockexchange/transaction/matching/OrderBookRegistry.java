package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.transaction.matching.feed.TradeFeed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Order books of all tickers with open transactions. Subscribes the trade feed to a ticker when its first transaction is added and unsubscribes it
 * when the last one is removed. All methods are synchronized, so the matching job and the transaction events can use it at the same time.
 */
@Component
public class OrderBookRegistry {

    private final Map<String, OrderBook> orderBooks = new HashMap<>();

    private final Map<Long, String> tickersByTransactionId = new HashMap<>();

    private final TradeFeed tradeFeed;

    private final BigDecimal participationRate;

    public OrderBookRegistry(final TradeFeed tradeFeed, @Value("${transaction.matching.participation-rate:1}") final BigDecimal participationRate) {
        if (participationRate.signum() <= 0 || participationRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Participation rate must be bigger than 0 and not bigger than 1, but was: " + participationRate);
        }

        this.tradeFeed = tradeFeed;
        this.participationRate = participationRate;
    }

    public synchronized void add(final OrderBookEntry entry) {
        remove(entry.getTransactionId());

        if (entry.getRemainingQuantity().signum() <= 0) {
            return;
        }

        final OrderBook orderBook = orderBooks.computeIfAbsent(entry.getTicker(), ticker -> {
            tradeFeed.subscribe(ticker);
            return new OrderBook(ticker);
        });

        orderBook.add(entry);
        tickersByTransactionId.put(entry.getTransactionId(), entry.getTicker());
    }

    public synchronized void remove(final long transactionId) {
        final String ticker = tickersByTransactionId.remove(transactionId);

        if (ticker == null) {
            return;
        }

        final OrderBook orderBook = orderBooks.get(ticker);
        orderBook.remove(transactionId);
        removeIfEmpty(orderBook);
    }

    public synchronized void clear() {
        orderBooks.keySet().forEach(tradeFeed::unsubscribe);
        orderBooks.clear();
        tickersByTransactionId.clear();
    }

    public synchronized List<Fill> match(final String ticker, final List<Trade> trades) {
        final OrderBook orderBook = orderBooks.get(ticker);

        if (orderBook == null) {
            return List.of();
        }

        final List<Fill> fills = orderBook.match(trades, participationRate);

        tickersByTransactionId.entrySet().removeIf(entry -> ticker.equals(entry.getValue()) && !orderBook.contains(entry.getKey()));
        removeIfEmpty(orderBook);

        return fills;
    }

    public synchronized boolean contains(final long transactionId) {
        return tickersByTransactionId.containsKey(transactionId);
    }

    public synchronized Set<String> getTickers() {
        return Set.copyOf(orderBooks.keySet());
    }

    private void removeIfEmpty(final OrderBook orderBook) {
        if (orderBook.isEmpty()) {
            orderBooks.remove(orderBook.getTicker());
            tradeFeed.unsubscribe(orderBook.getTicker());
        }
    }
}
