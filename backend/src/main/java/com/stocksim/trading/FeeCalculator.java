package com.stocksim.trading;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
class FeeCalculator {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private final TradingProperties.Fees fees;

	FeeCalculator(TradingProperties properties) {
		this.fees = properties.fees();
	}

	BigDecimal fee(BigDecimal qty, BigDecimal gross) {
		BigDecimal fee = fees.perOrder()
			.add(fees.perShare().multiply(qty))
			.add(fees.percent().multiply(gross).divide(HUNDRED, 6, RoundingMode.HALF_UP));
		return fee.max(fees.minimum()).setScale(2, RoundingMode.HALF_UP);
	}
}
