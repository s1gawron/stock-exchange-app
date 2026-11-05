package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.transaction.matching.feed.TradeBuffer;
import com.s1gawron.stockexchange.transaction.service.TransactionFillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Takes the trades received since the last run, matches them against the order books and saves the fills, one database transaction per wallet.
 */
@Component
public class TransactionMatchingJob {

    private static final Logger log = LoggerFactory.getLogger(TransactionMatchingJob.class);

    private final TradeBuffer tradeBuffer;

    private final OrderBookRegistry orderBookRegistry;

    private final OrderBookSynchronizer orderBookSynchronizer;

    private final TransactionFillService transactionFillService;

    public TransactionMatchingJob(final TradeBuffer tradeBuffer, final OrderBookRegistry orderBookRegistry, final OrderBookSynchronizer orderBookSynchronizer,
        final TransactionFillService transactionFillService) {
        this.tradeBuffer = tradeBuffer;
        this.orderBookRegistry = orderBookRegistry;
        this.orderBookSynchronizer = orderBookSynchronizer;
        this.transactionFillService = transactionFillService;
    }

    @Scheduled(fixedDelayString = "${transaction.matching.interval-ms:1000}")
    public void matchTrades() {
        final List<Trade> trades = tradeBuffer.drain();

        if (trades.isEmpty()) {
            return;
        }

        final Map<String, List<Trade>> tradesByTicker = trades.stream()
            .collect(Collectors.groupingBy(Trade::ticker, LinkedHashMap::new, Collectors.toList()));
        final List<Fill> fills = new ArrayList<>();

        tradesByTicker.forEach((ticker, tickerTrades) -> fills.addAll(orderBookRegistry.match(ticker, tickerTrades)));

        if (fills.isEmpty()) {
            return;
        }

        final Map<Long, List<Fill>> fillsByWallet = fills.stream()
            .collect(Collectors.groupingBy(Fill::walletId, LinkedHashMap::new, Collectors.toList()));

        fillsByWallet.forEach(this::applyFills);
    }

    private void applyFills(final long walletId, final List<Fill> walletFills) {
        try {
            final List<Long> transactionIdsToReload = transactionFillService.applyFills(walletId, walletFills);

            if (!transactionIdsToReload.isEmpty()) {
                orderBookSynchronizer.reload(transactionIdsToReload);
            }
        } catch (final RuntimeException e) {
            log.error("Could not save fills of wallet#{}, order book entries are read again from the database", walletId, e);
            orderBookSynchronizer.reload(walletFills.stream().map(Fill::transactionId).toList());
        }
    }
}
