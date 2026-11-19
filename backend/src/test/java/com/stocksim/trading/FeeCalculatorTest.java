package com.stocksim.trading;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class FeeCalculatorTest {

	private static FeeCalculator calc(String perOrder, String perShare, String percent, String minimum) {
		return new FeeCalculator(new TradingProperties(new BigDecimal("0.05"), new TradingProperties.Fees(
				new BigDecimal(perOrder), new BigDecimal(perShare), new BigDecimal(percent), new BigDecimal(minimum))));
	}

	@Test
	void zeroByDefault() {
		assertThat(calc("0", "0", "0", "0").fee(BigDecimal.TEN, new BigDecimal("2000"))).isEqualByComparingTo("0");
	}

	@Test
	void perShareWithMinimum() {
		var c = calc("0", "0.005", "0", "1");
		assertThat(c.fee(BigDecimal.TEN, new BigDecimal("2000"))).isEqualByComparingTo("1.00"); // 0.05 -> min 1
		assertThat(c.fee(BigDecimal.valueOf(1000), new BigDecimal("20000"))).isEqualByComparingTo("5.00");
	}

	@Test
	void percentAndPerOrder() {
		assertThat(calc("1", "0", "0.1", "0").fee(BigDecimal.ONE, new BigDecimal("2000")))
			.isEqualByComparingTo("3.00"); // 1 + 0.1% of 2000
	}
}
