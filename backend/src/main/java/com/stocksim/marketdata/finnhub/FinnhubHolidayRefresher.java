package com.stocksim.marketdata.finnhub;

import com.stocksim.marketdata.MarketDataUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

/** Keeps the fallback calendar's holidays current from Finnhub: at startup and daily. */
class FinnhubHolidayRefresher {

	private static final Logger log = LoggerFactory.getLogger(FinnhubHolidayRefresher.class);

	private final FinnhubMarketStatus marketStatus;

	FinnhubHolidayRefresher(FinnhubMarketStatus marketStatus) {
		this.marketStatus = marketStatus;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(cron = "0 30 5 * * *", zone = "America/New_York")
	void refresh() {
		try {
			log.info("Loaded {} US market holiday/early-close days from Finnhub", marketStatus.refreshHolidays());
		}
		catch (MarketDataUnavailableException ex) {
			log.warn("Finnhub holidays not loaded ({}); using configured list", ex.getMessage());
		}
	}
}
