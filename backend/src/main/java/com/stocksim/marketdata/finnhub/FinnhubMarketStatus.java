package com.stocksim.marketdata.finnhub;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.MarketStatus;
import com.stocksim.marketdata.UsMarketCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Live US market status from Finnhub, cached for {@code ttl}. When Finnhub is unreachable or over the rate limit,
 * falls back to the local {@link UsMarketCalendar}, so an outage neither opens nor blocks trading by mistake.
 */
class FinnhubMarketStatus {

	private static final Logger log = LoggerFactory.getLogger(FinnhubMarketStatus.class);

	private record Cached(MarketStatus status, Instant fetchedAt) {
	}

	private final FinnhubClient client;
	private final UsMarketCalendar calendar;
	private final Clock clock;
	private final Duration ttl;
	private volatile Cached cached;

	FinnhubMarketStatus(FinnhubClient client, UsMarketCalendar calendar, Clock clock, Duration ttl) {
		this.client = client;
		this.calendar = calendar;
		this.clock = clock;
		this.ttl = ttl;
	}

	MarketStatus current() {
		Cached c = cached;
		Instant now = clock.instant();
		if (c != null && c.fetchedAt().plus(ttl).isAfter(now)) {
			return c.status();
		}
		try {
			MarketStatus status = map(client.marketStatus("US"));
			cached = new Cached(status, now);
			return status;
		}
		catch (MarketDataUnavailableException | IllegalStateException ex) {
			log.warn("Finnhub market status unavailable ({}); using local calendar", ex.getMessage());
			return calendar.status();
		}
	}

	/** Loads Finnhub's holiday list into the fallback calendar. Returns the number of days loaded. */
	int refreshHolidays() {
		FinnhubClient.HolidayResponse r = client.marketHolidays("US");
		if (r == null || r.data() == null) {
			return 0;
		}
		Set<LocalDate> holidays = new HashSet<>();
		Map<LocalDate, LocalTime> earlyCloses = new HashMap<>();
		for (FinnhubClient.HolidayResponse.Holiday h : r.data()) {
			try {
				LocalDate day = LocalDate.parse(h.atDate());
				if (h.tradingHour() == null || h.tradingHour().isBlank()) {
					holidays.add(day);
				}
				else {
					// "09:30-13:00" -> close at 13:00
					earlyCloses.put(day, LocalTime.parse(h.tradingHour().substring(h.tradingHour().indexOf('-') + 1)));
				}
			}
			catch (DateTimeParseException | NullPointerException ex) {
				log.warn("Skipping unparseable Finnhub holiday entry: {}", h);
			}
		}
		calendar.addVendorDays(holidays, earlyCloses);
		return holidays.size() + earlyCloses.size();
	}

	static MarketStatus map(FinnhubClient.MarketStatusResponse r) {
		if (r == null || r.isOpen() == null) {
			throw new IllegalStateException("Empty market status response");
		}
		String session = r.session() == null ? "" : r.session();
		// Only the regular session counts as open, even if isOpen were true in extended hours.
		if (r.isOpen() && (session.isEmpty() || session.equals("regular"))) {
			return new MarketStatus(true, "Open");
		}
		if (r.holiday() != null && !r.holiday().isBlank()) {
			return new MarketStatus(false, "Closed (" + r.holiday() + ")");
		}
		return switch (session) {
			case "pre-market" -> new MarketStatus(false, "Closed (pre-market)");
			case "post-market" -> new MarketStatus(false, "Closed (after hours)");
			default -> new MarketStatus(false, "Closed");
		};
	}
}
