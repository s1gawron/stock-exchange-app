package com.stocksim.performance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.stocksim.marketdata.MarketDataProperties;
import com.stocksim.marketdata.UsMarketCalendar;

import org.junit.jupiter.api.Test;

class SnapshotJobTest {

	private final SnapshotService snapshots = mock(SnapshotService.class);

	private SnapshotJob jobAt(String instant) {
		Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
		var calendar = new UsMarketCalendar(clock, new MarketDataProperties.UsCalendar(
				List.of(LocalDate.parse("2026-11-26")), List.of(LocalDate.parse("2026-11-27"))));
		return new SnapshotJob(snapshots, calendar, clock, true);
	}

	@Test
	void takesMissingSnapshotAfterCloseBeforeNextOpen() {
		// Thu 2026-11-19 22:00 Warsaw = 16:00 NY + 1h -> day 2026-11-19
		assertThat(jobAt("2026-11-19T22:00:00Z").runIfDue()).isTrue();
		verify(snapshots).snapshot(LocalDate.parse("2026-11-19"));
	}

	@Test
	void skipsWhenAlreadyTaken() {
		when(snapshots.exists(LocalDate.parse("2026-11-19"))).thenReturn(true);
		assertThat(jobAt("2026-11-19T22:00:00Z").runIfDue()).isFalse();
		verify(snapshots, never()).snapshot(any());
	}

	@Test
	void skipsWhenNextSessionAlreadyOpened() {
		// Fri 2026-11-20 15:00 UTC = 10:00 NY: Thursday's snapshot can no longer be taken
		assertThat(jobAt("2026-11-20T15:00:00Z").runIfDue()).isFalse();
		verify(snapshots, never()).snapshot(any());
	}

	@Test
	void weekendCatchUpUsesFridayAndHolidayIsSkipped() {
		jobAt("2026-11-22T12:00:00Z").runIfDue(); // Sunday
		verify(snapshots).snapshot(LocalDate.parse("2026-11-20"));
		jobAt("2026-11-26T22:00:00Z").runIfDue(); // Thanksgiving evening -> Wednesday
		verify(snapshots).snapshot(LocalDate.parse("2026-11-25"));
		jobAt("2026-11-27T18:30:00Z").runIfDue(); // Friday after 13:00 early close
		verify(snapshots).snapshot(LocalDate.parse("2026-11-27"));
	}
}
