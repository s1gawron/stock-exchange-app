package com.s1gawron.stockexchange.transaction.matching;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Quantity of one transaction filled in one matching run. The price is the volume weighted average price of the trades used for the fill.
 *
 * @param lastTradeTimestamp time of the last trade used for the fill
 */
public record Fill(long transactionId, long walletId, BigDecimal quantity, BigDecimal price, Instant lastTradeTimestamp) {

}
