package com.stocksim.marketdata;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param provider       {@code finnhub} (default) or {@code fake}
 * @param quoteCacheTtl  how long a fetched quote is reused from Redis
 * @param maxQuoteAge    market orders are rejected when the quote's last trade is older than this
 * @param importTypes    instrument types kept from the vendor symbol list
 * @param usCalendar     NYSE holidays and early closes (verify yearly against the official NYSE calendar)
 * @param fake           settings for the fake provider
 */
@ConfigurationProperties(prefix = "marketdata")
public record MarketDataProperties(@DefaultValue("finnhub") String provider,
		@DefaultValue("5s") Duration quoteCacheTtl,
		@DefaultValue("15m") Duration maxQuoteAge,
		@DefaultValue({ "Common Stock", "ETP", "ADR", "REIT" }) List<String> importTypes,
		@DefaultValue UsCalendar usCalendar,
		@DefaultValue Fake fake) {

	public record UsCalendar(@DefaultValue List<LocalDate> holidays, @DefaultValue List<LocalDate> earlyCloses) {
	}

	/** @param marketOpen what the fake provider reports as market status, so trading can be tested any time */
	public record Fake(@DefaultValue("true") boolean marketOpen) {
	}
}
