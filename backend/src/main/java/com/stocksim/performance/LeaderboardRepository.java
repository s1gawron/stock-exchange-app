package com.stocksim.performance;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Read-only ranking query. DOCUMENTED ARCHITECTURE EXCEPTION: one SQL query joins {@code account_snapshots}
 * (performance) with {@code accounts} and {@code users} (other modules) for performance. It never writes. The list
 * of such exceptions is in ArchitectureTest.
 */
@Repository
class LeaderboardRepository {

	record RankedRow(int rank, long userId, String displayName, BigDecimal returnPercent, BigDecimal equity) {
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

	LeaderboardRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	List<RankedRow> top(LocalDate day, int limit) {
		return jdbc.sql("SELECT * FROM (" + RANKED + ") r ORDER BY rnk, display_name LIMIT :limit")
			.param("day", Date.valueOf(day))
			.param("limit", limit)
			.query((rs, n) -> map(rs))
			.list();
	}

	Optional<RankedRow> find(LocalDate day, long userId) {
		return jdbc.sql("SELECT * FROM (" + RANKED + ") r WHERE user_id = :me")
			.param("day", Date.valueOf(day))
			.param("me", userId)
			.query((rs, n) -> map(rs))
			.optional();
	}

	private static RankedRow map(java.sql.ResultSet rs) throws java.sql.SQLException {
		return new RankedRow(rs.getInt("rnk"), rs.getLong("user_id"), rs.getString("display_name"),
				rs.getBigDecimal("return_pct"), rs.getBigDecimal("equity"));
	}
}
