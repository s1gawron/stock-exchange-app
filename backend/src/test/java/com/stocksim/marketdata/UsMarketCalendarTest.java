package com.stocksim.marketdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UsMarketCalendarTest {

	private static final MarketDataProperties.UsCalendar CONFIG = new MarketDataProperties.UsCalendar(
			List.of(LocalDate.parse("2026-11-26")), List.of(LocalDate.parse("2026-11-27")));

	private static MarketStatus statusAt(String newYorkLocalTime) {
		var at = LocalDateTime.parse(newYorkLocalTime).atZone(UsMarketCalendar.NEW_YORK).toInstant();
		return new UsMarketCalendar(Clock.fixed(at, UsMarketCalendar.NEW_YORK), CONFIG).status();
	}

	@ParameterizedTest
	@CsvSource({
			"2026-11-19T09:29:59, false, Closed (pre-market)",
			"2026-11-19T09:30:00, true,  Open",
			"2026-11-19T15:59:59, true,  Open",
			"2026-11-19T16:00:00, false, Closed (after hours)",
			"2026-11-21T12:00:00, false, Closed (weekend)",
			"2026-11-26T12:00:00, false, Closed (holiday)",
			"2026-11-27T12:59:59, true,  Open",
			"2026-11-27T13:00:00, false, Closed (after hours)" })
	void regularSessionRules(String time, boolean open, String description) {
		assertThat(statusAt(time)).isEqualTo(new MarketStatus(open, description));
	}

	@Test
	void usesNewYorkTimeNotUtc() {
		// 14:45 UTC = 09:45 New York (EST, UTC-5) -> open
		var clock = Clock.fixed(java.time.Instant.parse("2026-11-19T14:45:00Z"), java.time.ZoneOffset.UTC);
		assertThat(new UsMarketCalendar(clock, CONFIG).status().open()).isTrue();
	}

	@ParameterizedTest
	@CsvSource({
			// during session -> today's close (EST, UTC-5)
			"2026-11-19T10:00:00, 2026-11-19T21:00:00Z",
			// before open -> today's close
			"2026-11-19T08:00:00, 2026-11-19T21:00:00Z",
			// after close Wednesday -> skips Thanksgiving holiday, Friday early close 13:00
			"2026-11-25T17:00:00, 2026-11-27T18:00:00Z",
			// Saturday -> Monday close
			"2026-11-28T12:00:00, 2026-11-30T21:00:00Z" })
	void nextSessionClose(String newYorkTime, String expected) {
		var at = LocalDateTime.parse(newYorkTime).atZone(UsMarketCalendar.NEW_YORK).toInstant();
		var calendar = new UsMarketCalendar(Clock.systemUTC(), CONFIG);
		assertThat(calendar.nextSessionClose(at)).isEqualTo(java.time.Instant.parse(expected));
	}
}
