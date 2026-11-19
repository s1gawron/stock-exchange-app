package com.stocksim.marketdata.fake;

import java.time.Clock;

import com.stocksim.marketdata.MarketDataProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "marketdata.provider", havingValue = "fake")
class FakeMarketDataConfig {

	@Bean
	FakeMarketDataProvider fakeMarketDataProvider(Clock clock, MarketDataProperties properties) {
		return new FakeMarketDataProvider(clock, properties.fake());
	}
}
