package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.shared.QuantityRounding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Open transactions of one ticker, matched against real market trades with price-time priority.
 * <p>
 * Each trade with price P and volume V gives two separate pools of {@code V * participationRate} shares at price P:
 * <ul>
 *     <li>purchases with price limit >= P share the first pool, highest limit first, then oldest first,</li>
 *     <li>sells with price limit <= P share the second pool, lowest limit first, then oldest first.</li>
 * </ul>
 * The pools are separate because a simulated purchase takes the place of the real buyer and a simulated sell takes the place of the real seller.
 * <p>
 * This class is not thread safe.
 */
public class OrderBook {

    private static final Comparator<OrderBookEntry> TIME_PRIORITY = Comparator.comparing(OrderBookEntry::getCreateDate)
        .thenComparingLong(OrderBookEntry::getTransactionId);

    private static final Comparator<OrderBookEntry> PURCHASE_PRIORITY = Comparator.comparing(OrderBookEntry::getPriceLimit).reversed()
        .thenComparing(TIME_PRIORITY);

    private static final Comparator<OrderBookEntry> SELL_PRIORITY = Comparator.comparing(OrderBookEntry::getPriceLimit).thenComparing(TIME_PRIORITY);

    private final String ticker;

    private final NavigableSet<OrderBookEntry> purchases = new TreeSet<>(PURCHASE_PRIORITY);

    private final NavigableSet<OrderBookEntry> sells = new TreeSet<>(SELL_PRIORITY);

    private final Map<Long, OrderBookEntry> entriesById = new HashMap<>();

    public OrderBook(final String ticker) {
        this.ticker = ticker;
    }

    public String getTicker() {
        return ticker;
    }

    public void add(final OrderBookEntry entry) {
        if (!ticker.equals(entry.getTicker())) {
            throw new IllegalArgumentException("Entry for ticker: " + entry.getTicker() + " cannot be added to order book of: " + ticker);
        }

        remove(entry.getTransactionId());

        if (entry.isFilled()) {
            return;
        }

        entriesById.put(entry.getTransactionId(), entry);
        sideOf(entry).add(entry);
    }

    public void remove(final long transactionId) {
        final OrderBookEntry entry = entriesById.remove(transactionId);

        if (entry != null) {
            sideOf(entry).remove(entry);
        }
    }

    public boolean contains(final long transactionId) {
        return entriesById.containsKey(transactionId);
    }

    public boolean isEmpty() {
        return entriesById.isEmpty();
    }

    public int size() {
        return entriesById.size();
    }

    /**
     * Matches the trades in time order and returns one fill per transaction. Fully filled transactions are removed from the book.
     */
    public List<Fill> match(final List<Trade> trades, final BigDecimal participationRate) {
        final Map<Long, FillAccumulator> fills = new LinkedHashMap<>();
        final List<Trade> tradesInTimeOrder = trades.stream()
            .filter(trade -> ticker.equals(trade.ticker()))
            .filter(trade -> trade.price() != null && trade.price().signum() > 0)
            .filter(trade -> trade.volume() != null && trade.volume().signum() > 0)
            .sorted(Comparator.comparing(Trade::timestamp))
            .toList();

        for (final Trade trade : tradesInTimeOrder) {
            final BigDecimal pool = QuantityRounding.I.quantity(trade.volume().multiply(participationRate));

            matchSide(purchases, trade, pool, entry -> entry.getPriceLimit().compareTo(trade.price()) >= 0, fills);
            matchSide(sells, trade, pool, entry -> entry.getPriceLimit().compareTo(trade.price()) <= 0, fills);
        }

        return fills.values().stream()
            .map(FillAccumulator::toFill)
            .toList();
    }

    private void matchSide(final NavigableSet<OrderBookEntry> side, final Trade trade, final BigDecimal pool,
        final Predicate<OrderBookEntry> priceMatches, final Map<Long, FillAccumulator> fills) {
        BigDecimal poolLeft = pool;
        final List<OrderBookEntry> filledEntries = new ArrayList<>();
        final Iterator<OrderBookEntry> iterator = side.iterator();

        while (poolLeft.signum() > 0 && iterator.hasNext()) {
            final OrderBookEntry entry = iterator.next();

            // The side is sorted by price limit, so no later entry can match when this one does not.
            if (!priceMatches.test(entry)) {
                break;
            }

            final BigDecimal quantity = poolLeft.min(entry.getRemainingQuantity());

            entry.decreaseRemainingQuantity(quantity);
            poolLeft = poolLeft.subtract(quantity);
            fills.computeIfAbsent(entry.getTransactionId(), id -> new FillAccumulator(entry)).add(quantity, trade);

            if (entry.isFilled()) {
                filledEntries.add(entry);
            }
        }

        filledEntries.forEach(entry -> remove(entry.getTransactionId()));
    }

    private NavigableSet<OrderBookEntry> sideOf(final OrderBookEntry entry) {
        return entry.getTransactionType().isPurchase() ? purchases : sells;
    }

    private static final class FillAccumulator {

        private final OrderBookEntry entry;

        private BigDecimal quantity = BigDecimal.ZERO;

        private BigDecimal value = BigDecimal.ZERO;

        private Instant lastTradeTimestamp;

        private FillAccumulator(final OrderBookEntry entry) {
            this.entry = entry;
        }

        private void add(final BigDecimal fillQuantity, final Trade trade) {
            this.quantity = this.quantity.add(fillQuantity);
            this.value = this.value.add(trade.price().multiply(fillQuantity));
            this.lastTradeTimestamp = trade.timestamp();
        }

        private Fill toFill() {
            final BigDecimal averagePrice = value.divide(quantity, MoneyRounding.MONEY_SCALE, RoundingMode.HALF_UP);
            return new Fill(entry.getTransactionId(), entry.getWalletId(), QuantityRounding.I.quantity(quantity), averagePrice, lastTradeTimestamp);
        }
    }
}
