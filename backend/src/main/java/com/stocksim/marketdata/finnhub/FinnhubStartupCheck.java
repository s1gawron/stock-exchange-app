package com.stocksim.marketdata.finnhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Warns at startup when no Finnhub API key is set, so a missing key is visible early. */
@Component
class FinnhubStartupCheck {

	private static final Logger log = LoggerFactory.getLogger(FinnhubStartupCheck.class);

	private final FinnhubProperties properties;

	FinnhubStartupCheck(FinnhubProperties properties) {
		this.properties = properties;
	}

	@EventListener(ApplicationReadyEvent.class)
	void check() {
		if (!properties.isConfigured()) {
			log.warn("FINNHUB_API_KEY is not set. Market data calls will fail. See .env.example.");
		}
	}
}
