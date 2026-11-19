package com.stocksim.marketdata.finnhub;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Finnhub connection settings. The API key comes from FINNHUB_API_KEY (or the repo-root .env). It is sent only in
 * the {@code X-Finnhub-Token} header, never in URLs or logs, and never to the browser.
 *
 * @param rateLimitPerMinute local cap on REST calls; keep it below the limit of our Finnhub plan (free tier: 60/min)
 * @param marketStatusTtl    how long a fetched market status is reused
 * @param websocketMaxSymbols max symbols followed on the WebSocket (free tier accepts 51, measured 2026-11-19)
 */
@ConfigurationProperties(prefix = "finnhub")
public record FinnhubProperties(String apiKey,
		@DefaultValue("https://finnhub.io/api/v1") String restBaseUrl,
		@DefaultValue("wss://ws.finnhub.io") String websocketUrl,
		@DefaultValue("55") int rateLimitPerMinute,
		@DefaultValue("2s") Duration rateLimitWait,
		@DefaultValue("10s") Duration timeout,
		@DefaultValue("60s") Duration marketStatusTtl,
		@DefaultValue("50") int websocketMaxSymbols) {

	public boolean isConfigured() {
		return apiKey != null && !apiKey.isBlank();
	}

	@Override
	public String toString() {
		return "FinnhubProperties[apiKey=" + (isConfigured() ? "***" : "<missing>") + ", restBaseUrl=" + restBaseUrl
				+ ", rateLimitPerMinute=" + rateLimitPerMinute + "]";
	}
}
