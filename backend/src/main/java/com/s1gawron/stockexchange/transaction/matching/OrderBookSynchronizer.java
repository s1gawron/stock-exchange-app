package com.s1gawron.stockexchange.transaction.matching;

import com.s1gawron.stockexchange.transaction.dao.TransactionDAO;
import com.s1gawron.stockexchange.transaction.matching.event.TransactionClosedEvent;
import com.s1gawron.stockexchange.transaction.matching.event.TransactionOpenedEvent;
import com.s1gawron.stockexchange.transaction.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collection;

/**
 * Keeps the in-memory order books equal to the open transactions in the database.
 */
@Component
public class OrderBookSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(OrderBookSynchronizer.class);

    private final OrderBookRegistry orderBookRegistry;

    private final TransactionDAO transactionDAO;

    public OrderBookSynchronizer(final OrderBookRegistry orderBookRegistry, final TransactionDAO transactionDAO) {
        this.orderBookRegistry = orderBookRegistry;
        this.transactionDAO = transactionDAO;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void loadOpenTransactions() {
        orderBookRegistry.clear();
        transactionDAO.getOpenTransactions().forEach(transaction -> orderBookRegistry.add(OrderBookEntry.from(transaction)));

        log.info("Loaded open transactions into order books of {} tickers", orderBookRegistry.getTickers().size());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionOpened(final TransactionOpenedEvent event) {
        orderBookRegistry.add(event.entry());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionClosed(final TransactionClosedEvent event) {
        orderBookRegistry.remove(event.transactionId());
    }

    /**
     * Reads the transactions from the database again and replaces their order book entries. Used when a fill could not be saved.
     */
    @Transactional(readOnly = true)
    public void reload(final Collection<Long> transactionIds) {
        for (final long transactionId : transactionIds) {
            orderBookRegistry.remove(transactionId);
            transactionDAO.getTransactionById(transactionId)
                .filter(Transaction::isOpen)
                .ifPresent(transaction -> orderBookRegistry.add(OrderBookEntry.from(transaction)));
        }
    }
}
