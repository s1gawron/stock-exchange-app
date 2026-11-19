package com.stocksim.performance;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

import com.stocksim.marketdata.UsMarketCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Takes the daily snapshot at 16:30 New York on trading days. On startup it catches up the last closed day, but only
 * before the next session opens: later, current prices would no longer be that day's close.
 */
@Component
class SnapshotJob {

	private static final Logger log = LoggerFactory.getLogger(SnapshotJob.class);

	private final SnapshotService snapshots;
	private final UsMarketCalendar calendar;
	private final Clock clock;
	private final boolean catchUpOnStartup;

	SnapshotJob(SnapshotService snapshots, UsMarketCalendar calendar, Clock clock,
			@Value("${performance.catch-up-on-startup:true}") boolean catchUpOnStartup) {
		this.snapshots = snapshots;
		this.calendar = calendar;
		this.clock = clock;
		this.catchUpOnStartup = catchUpOnStartup;
	}

	@Scheduled(cron = "${performance.snapshot-cron:0 30 16 * * MON-FRI}", zone = "America/New_York")
	void afterClose() {
		runIfDue();
	}

	@EventListener(ApplicationReadyEvent.class)
	void catchUpOnStartup() {
		if (catchUpOnStartup) {
			runIfDue();
		}
	}

	/** Snapshots the last closed trading day if it is missing and the next session has not opened yet. */
	boolean runIfDue() {
		Instant now = clock.instant();
		LocalDate day = calendar.lastClosedSessionDay(now);
		if (snapshots.exists(day)) {
			return false;
		}
		if (!now.isBefore(calendar.nextSessionOpenAfter(day))) {
			log.warn("Snapshot for {} missed (server was down); it cannot be taken with today's prices", day);
			return false;
		}
		snapshots.snapshot(day);
		return true;
	}
}
