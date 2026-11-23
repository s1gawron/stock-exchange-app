package com.stocksim.trading;

import java.math.BigDecimal;

import com.stocksim.portfolio.RealizedPnlSource;

import org.springframework.stereotype.Component;

/** Gives the portfolio module the realized P&L of an account, read from the fills. */
@Component
class TradingRealizedPnl implements RealizedPnlSource {

	private final OrderRepository orders;

	TradingRealizedPnl(OrderRepository orders) {
		this.orders = orders;
	}

	@Override
	public BigDecimal realizedPnl(long accountId) {
		return orders.realizedPnl(accountId);
	}
}
