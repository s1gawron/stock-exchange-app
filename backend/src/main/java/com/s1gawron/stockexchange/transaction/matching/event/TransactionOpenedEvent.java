package com.s1gawron.stockexchange.transaction.matching.event;

import com.s1gawron.stockexchange.transaction.matching.OrderBookEntry;

/**
 * Published when a transaction is created. After commit, the transaction is added to the order book.
 */
public record TransactionOpenedEvent(OrderBookEntry entry) {

}
