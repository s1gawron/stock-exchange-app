package com.stocksim.trading;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderView(long id, String symbol, OrderSide side, String type, BigDecimal qty, String status,
		RejectReason rejectReason, Instant createdAt, Fill fill) {

	public record Fill(BigDecimal price, BigDecimal grossAmount, BigDecimal fee, BigDecimal realizedPnl,
			Instant quoteTime, Instant executedAt) {
	}
}
