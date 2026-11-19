package com.stocksim.marketdata;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
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

	/** Close of the session that is open now, or of the next session when the market is closed. */
	public Instant nextSessionClose(Instant from) {
		LocalDate day = from.atZone(NEW_YORK).toLocalDate();
		for (int i = 0; i < 30; i++) {
			LocalDate d = day.plusDays(i);
			if (isTradingDay(d)) {
				Instant close = d.atTime(earlyCloses.getOrDefault(d, CLOSE)).atZone(NEW_YORK).toInstant();
				if (close.isAfter(from)) {
					return close;
				}
			}
		}
		throw new IllegalStateException("No trading day in the next 30 days");
	}

	public boolean isTradingDay(LocalDate d) {
		return d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(d);
	}

	/** Close time of a trading day (16:00 New York, earlier on early-close days). */
	public Instant closeOf(LocalDate tradingDay) {
		return tradingDay.atTime(earlyCloses.getOrDefault(tradingDay, CLOSE)).atZone(NEW_YORK).toInstant();
	}

	/** Most recent trading day whose session has already closed at {@code now}. */
	public LocalDate lastClosedSessionDay(Instant now) {
		LocalDate d = now.atZone(NEW_YORK).toLocalDate();
		for (int i = 0; i < 30; i++, d = d.minusDays(1)) {
			if (isTradingDay(d) && !closeOf(d).isAfter(now)) {
				return d;
			}
		}
		throw new IllegalStateException("No closed trading day in the last 30 days");
	}

	/** Open of the first trading session after {@code tradingDay}. */
	public Instant nextSessionOpenAfter(LocalDate tradingDay) {
		LocalDate d = tradingDay.plusDays(1);
		for (int i = 0; i < 30; i++, d = d.plusDays(1)) {
			if (isTradingDay(d)) {
				return d.atTime(OPEN).atZone(NEW_YORK).toInstant();
			}
		}
		throw new IllegalStateException("No trading day in the next 30 days");
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
