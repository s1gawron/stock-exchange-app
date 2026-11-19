package com.stocksim.trading;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param stopBuyReserveBuffer extra cash reserved for BUY STOP orders on top of qty * stop price, because they fill
 *                             at the market price after the trigger (0.05 = 5%)
 */
@ConfigurationProperties(prefix = "trading")
public record TradingProperties(@DefaultValue("0.05") BigDecimal stopBuyReserveBuffer, @DefaultValue Fees fees) {

	/**
	 * fee = max(minimum, perOrder + perShare * qty + percent / 100 * gross). All zero by default (commission-free).
	 * A buy fee is added to the cost basis; a sell fee is subtracted from realized P&L.
	 */
	public record Fees(@DefaultValue("0") BigDecimal perOrder, @DefaultValue("0") BigDecimal perShare,
			@DefaultValue("0") BigDecimal percent, @DefaultValue("0") BigDecimal minimum) {
	}
}
