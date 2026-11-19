package com.stocksim.marketdata.finnhub;

import java.util.List;

import com.stocksim.marketdata.MarketDataUnavailableException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Thin typed wrapper over the Finnhub REST API. All calls pass through the local rate limiter. */
class FinnhubClient {

	/** Finnhub sends null for some fields (e.g. d, dp for unknown symbols), so all are nullable. */
	record QuoteResponse(Double c, Double d, Double dp, Double h, Double l, Double o, Double pc, Long t) {
	}

	record SymbolResponse(String symbol, String displaySymbol, String description, String type, String currency,
			String mic, String figi) {
	}

	record ProfileResponse(String name, String exchange, String currency, String logo, String weburl,
			String finnhubIndustry, String ticker) {
	}

	/** {@code isOpen} is true only in the regular session (pre-market showed false). */
	record MarketStatusResponse(String exchange, String holiday, Boolean isOpen, String session, Long t,
			String timezone) {
	}

	/** {@code tradingHour} is empty for full-day closures, e.g. "09:30-13:00" for early closes. */
	record HolidayResponse(List<Holiday> data) {
		record Holiday(String eventName, String atDate, String tradingHour) {
		}
	}

	private final RestClient http;
	private final RateLimiter rateLimiter;
	private final FinnhubProperties properties;

	FinnhubClient(RestClient http, FinnhubProperties properties) {
		this.http = http;
		this.properties = properties;
		this.rateLimiter = new RateLimiter(properties.rateLimitPerMinute());
	}

	QuoteResponse quote(String symbol) {
		return call(() -> http.get().uri("/quote?symbol={s}", symbol).retrieve().body(QuoteResponse.class));
	}

	List<SymbolResponse> symbols(String exchange) {
		return call(() -> http.get()
			.uri("/stock/symbol?exchange={e}", exchange)
			.retrieve()
			.body(new ParameterizedTypeReference<List<SymbolResponse>>() {
			}));
	}

	ProfileResponse profile(String symbol) {
		return call(() -> http.get().uri("/stock/profile2?symbol={s}", symbol).retrieve().body(ProfileResponse.class));
	}

	MarketStatusResponse marketStatus(String exchange) {
		return call(() -> http.get()
			.uri("/stock/market-status?exchange={e}", exchange)
			.retrieve()
			.body(MarketStatusResponse.class));
	}

	HolidayResponse marketHolidays(String exchange) {
		return call(() -> http.get()
			.uri("/stock/market-holiday?exchange={e}", exchange)
			.retrieve()
			.body(HolidayResponse.class));
	}

	private <T> T call(java.util.function.Supplier<T> request) {
		if (!properties.isConfigured()) {
			throw new MarketDataUnavailableException("FINNHUB_API_KEY is not set");
		}
		if (!rateLimiter.tryAcquire(properties.rateLimitWait())) {
			throw new MarketDataUnavailableException("Finnhub rate limit reached, try again shortly");
		}
		try {
			return request.get();
		}
		catch (RestClientResponseException ex) {
			// Do not include the response body: keep vendor messages out of our API responses.
			throw new MarketDataUnavailableException("Finnhub returned HTTP " + ex.getStatusCode().value(), ex);
		}
		catch (RestClientException ex) {
			throw new MarketDataUnavailableException("Finnhub request failed", ex);
		}
	}
}
