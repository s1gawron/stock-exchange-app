package com.stocksim.marketdata.finnhub;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Finnhub connection settings. The API key comes from the FINNHUB_API_KEY env var (or the repo-root .env)
 * and must never be sent to the browser or logged.
 */
@ConfigurationProperties(prefix = "finnhub")
public record FinnhubProperties(String apiKey, String restBaseUrl, String websocketUrl) {

	public boolean isConfigured() {
		return apiKey != null && !apiKey.isBlank();
	}

	@Override
	public String toString() {
		return "FinnhubProperties[apiKey=" + (isConfigured() ? "***" : "<missing>")
				+ ", restBaseUrl=" + restBaseUrl + ", websocketUrl=" + websocketUrl + "]";
	}
}
