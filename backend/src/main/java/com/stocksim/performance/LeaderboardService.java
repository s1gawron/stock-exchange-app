package com.stocksim.performance;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/** Ranks all accounts by return since opening, as of the latest snapshot day. Shows display names only. */
@Service
public class LeaderboardService {

	public record Entry(int rank, String displayName, BigDecimal returnPercent, BigDecimal equity, boolean me) {
	}

	/** @param me the caller's entry, also when outside the top list; null before their first snapshot */
	public record Leaderboard(LocalDate asOf, int players, List<Entry> top, Entry me) {
	}

	private static final String RANKED = """
			SELECT u.id AS user_id, u.display_name, s.equity,
			       ROUND((s.equity - s.deposits) * 100 / NULLIF(s.deposits, 0), 2) AS return_pct,
			       RANK() OVER (ORDER BY (s.equity - s.deposits) / NULLIF(s.deposits, 0) DESC) AS rnk
			FROM account_snapshots s
			JOIN accounts a ON a.id = s.account_id
			JOIN users u ON u.id = a.user_id
			WHERE s.day = :day
			""";

	private final JdbcClient jdbc;

	LeaderboardService(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public Leaderboard get(long userId, int limit) {
		Optional<LocalDate> day = jdbc.sql("SELECT max(day) FROM account_snapshots")
			.query(Date.class)
			.optional()
			.map(Date::toLocalDate);
		if (day.isEmpty()) {
			return new Leaderboard(null, 0, List.of(), null);
		}
		Date d = Date.valueOf(day.get());
		List<Entry> top = jdbc.sql("SELECT * FROM (" + RANKED + ") r ORDER BY rnk, display_name LIMIT :limit")
			.param("day", d)
			.param("limit", limit)
			.query((rs, n) -> entry(rs, userId))
			.list();
		Entry me = jdbc.sql("SELECT * FROM (" + RANKED + ") r WHERE user_id = :me")
			.param("day", d)
			.param("me", userId)
			.query((rs, n) -> entry(rs, userId))
			.optional()
			.orElse(null);
		int players = jdbc.sql("SELECT count(*) FROM account_snapshots WHERE day = ?")
			.param(d)
			.query(Integer.class)
			.single();
		return new Leaderboard(day.get(), players, top, me);
	}

	private static Entry entry(java.sql.ResultSet rs, long userId) throws java.sql.SQLException {
		return new Entry(rs.getInt("rnk"), rs.getString("display_name"), rs.getBigDecimal("return_pct"),
				rs.getBigDecimal("equity"), rs.getLong("user_id") == userId);
	}
}
