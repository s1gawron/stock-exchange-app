package com.stocksim.performance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/** Ranks all accounts by return since opening, as of the latest snapshot day. Shows display names only. */
@Service
public class LeaderboardService {

	public record Entry(int rank, String displayName, BigDecimal returnPercent, BigDecimal equity, boolean me) {
	}

	/** @param me the caller's entry, also when outside the top list; null before their first snapshot */
	public record Leaderboard(LocalDate asOf, int players, List<Entry> top, Entry me) {
	}

	private final LeaderboardRepository ranking;
	private final SnapshotRepository snapshots;

	LeaderboardService(LeaderboardRepository ranking, SnapshotRepository snapshots) {
		this.ranking = ranking;
		this.snapshots = snapshots;
	}

	public Leaderboard get(long userId, int limit) {
		Optional<LocalDate> day = snapshots.latestDay();
		if (day.isEmpty()) {
			return new Leaderboard(null, 0, List.of(), null);
		}
		List<Entry> top = ranking.top(day.get(), limit).stream().map(r -> entry(r, userId)).toList();
		Entry me = ranking.find(day.get(), userId).map(r -> entry(r, userId)).orElse(null);
		int players = snapshots.countOn(day.get());
		return new Leaderboard(day.get(), players, top, me);
	}

	private static Entry entry(LeaderboardRepository.RankedRow r, long userId) {
		return new Entry(r.rank(), r.displayName(), r.returnPercent(), r.equity(), r.userId() == userId);
	}
}
