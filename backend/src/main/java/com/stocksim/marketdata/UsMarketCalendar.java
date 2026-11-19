package com.stocksim.marketdata;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * NYSE/Nasdaq regular session: 09:30–16:00 New York time, Mon–Fri, minus holidays; earlier close on early-close
 * days. Seeded from config, then extended with the vendor's holiday list ({@link #addVendorDays}). Used as the
 * fallback when the vendor's live market status is not available.
 */
public class UsMarketCalendar {

	public static final ZoneId NEW_YORK = ZoneId.of("America/New_York");

	private static final LocalTime OPEN = LocalTime.of(9, 30);
	private static final LocalTime CLOSE = LocalTime.of(16, 0);
	private static final LocalTime DEFAULT_EARLY_CLOSE = LocalTime.of(13, 0);

	private final Clock clock;
	private volatile Set<LocalDate> holidays;
	private volatile Map<LocalDate, LocalTime> earlyCloses;

	public UsMarketCalendar(Clock clock, MarketDataProperties.UsCalendar config) {
		this.clock = clock;
		this.holidays = Set.copyOf(config.holidays());
		Map<LocalDate, LocalTime> early = new HashMap<>();
		config.earlyCloses().forEach(d -> early.put(d, DEFAULT_EARLY_CLOSE));
		this.earlyCloses = Map.copyOf(early);
	}

	/** Merges vendor days into the calendar. Vendor data wins for days it knows. */
	public synchronized void addVendorDays(Set<LocalDate> vendorHolidays, Map<LocalDate, LocalTime> vendorEarlyCloses) {
		Set<LocalDate> h = new HashSet<>(holidays);
		Map<LocalDate, LocalTime> e = new HashMap<>(earlyCloses);
		vendorHolidays.forEach(d -> {
			h.add(d);
			e.remove(d);
		});
		vendorEarlyCloses.forEach((d, close) -> {
			h.remove(d);
			e.put(d, close);
		});
		holidays = Set.copyOf(h);
		earlyCloses = Map.copyOf(e);
	}

	public MarketStatus status() {
		ZonedDateTime now = ZonedDateTime.now(clock.withZone(NEW_YORK));
		LocalDate day = now.toLocalDate();
		LocalTime time = now.toLocalTime();
		if (day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return new MarketStatus(false, "Closed (weekend)");
		}
		if (holidays.contains(day)) {
			return new MarketStatus(false, "Closed (holiday)");
		}
		LocalTime close = earlyCloses.getOrDefault(day, CLOSE);
		if (time.isBefore(OPEN)) {
			return new MarketStatus(false, "Closed (pre-market)");
		}
		if (!time.isBefore(close)) {
			return new MarketStatus(false, "Closed (after hours)");
		}
		return new MarketStatus(true, "Open");
	}
}
