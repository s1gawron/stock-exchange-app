package com.s1gawron.stockexchange.transaction.matching.event;

/**
 * Published when a transaction is canceled. After commit, the transaction is removed from the order book.
 */
public record TransactionClosedEvent(long transactionId) {

}
