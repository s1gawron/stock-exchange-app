package com.stocksim.performance;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param benchmarkSymbol compared against in analytics (an S&P 500 ETF by default)
 * @param priceRetryDelay wait between price attempts while snapshotting (vendor rate limit)
 */
@ConfigurationProperties(prefix = "performance")
public record PerformanceProperties(@DefaultValue("SPY") String benchmarkSymbol,
		@DefaultValue("5s") Duration priceRetryDelay) {
}
