package com.stocksim.portfolio;

import java.math.BigDecimal;

/**
 * Supplies the realized P&L of an account. Implemented by the trading module, which owns the fills. Declared here so
 * that portfolio does not depend on trading (trading already depends on portfolio).
 */
public interface RealizedPnlSource {

	/** Sum of the realized P&L of all fills of the account (zero when there are none). */
	BigDecimal realizedPnl(long accountId);
}
