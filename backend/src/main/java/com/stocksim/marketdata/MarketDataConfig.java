package com.stocksim.marketdata;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class MarketDataConfig {

	@Bean
	UsMarketCalendar usMarketCalendar(Clock clock, MarketDataProperties properties) {
		return new UsMarketCalendar(clock, properties.usCalendar());
	}
}
