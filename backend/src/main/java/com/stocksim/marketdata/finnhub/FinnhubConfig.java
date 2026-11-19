package com.stocksim.marketdata.finnhub;

import java.time.Clock;

import com.stocksim.marketdata.MarketDataProvider;
import com.stocksim.marketdata.TickStore;
import com.stocksim.marketdata.UsMarketCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "marketdata.provider", havingValue = "finnhub", matchIfMissing = true)
class FinnhubConfig {

	private static final Logger log = LoggerFactory.getLogger(FinnhubConfig.class);

	@Bean
	FinnhubClient finnhubClient(RestClient.Builder builder, FinnhubProperties properties) {
		if (!properties.isConfigured()) {
			log.warn("FINNHUB_API_KEY is not set. Market data calls will fail. See .env.example.");
		}
		HttpClientSettings settings = HttpClientSettings.defaults()
			.withConnectTimeout(properties.timeout())
			.withReadTimeout(properties.timeout());
		RestClient http = builder.clone()
			.baseUrl(properties.restBaseUrl())
			.defaultHeader("X-Finnhub-Token", properties.isConfigured() ? properties.apiKey() : "")
			.requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
			.build();
		return new FinnhubClient(http, properties);
	}

	@Bean
	FinnhubMarketStatus finnhubMarketStatus(FinnhubClient client, UsMarketCalendar calendar, Clock clock,
			FinnhubProperties properties) {
		return new FinnhubMarketStatus(client, calendar, clock, properties.marketStatusTtl());
	}

	@Bean
	FinnhubHolidayRefresher finnhubHolidayRefresher(FinnhubMarketStatus marketStatus) {
		return new FinnhubHolidayRefresher(marketStatus);
	}

	@Bean
	FinnhubLiveFeed finnhubLiveFeed(FinnhubProperties properties, TickStore ticks, JsonMapper json) {
		return new FinnhubLiveFeed(properties, ticks, json);
	}

	@Bean
	MarketDataProvider finnhubMarketDataProvider(FinnhubClient client, FinnhubMarketStatus marketStatus) {
		return new FinnhubMarketDataProvider(client, marketStatus);
	}
}
