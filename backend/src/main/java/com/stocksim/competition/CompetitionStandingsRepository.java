package com.stocksim.competition;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Read-only standings query. DOCUMENTED ARCHITECTURE EXCEPTION: one SQL query joins {@code competition_members}
 * (competition) with {@code users}, {@code accounts}, {@code account_snapshots} and {@code ledger_entries} (other
 * modules) for performance. It never writes. The list of such exceptions is in ArchitectureTest.
 */
@Repository
class CompetitionStandingsRepository {

	record StandingRow(int rank, long userId, String displayName, BigDecimal returnPercent) {
	}

	private final JdbcClient jdbc;

	CompetitionStandingsRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	List<StandingRow> standings(long competitionId, LocalDate start, LocalDate end) {
		return jdbc.sql("""
				WITH m AS (
				  SELECT u.id AS user_id, u.display_name, a.id AS account_id
				  FROM competition_members cm JOIN users u ON u.id = cm.user_id JOIN accounts a ON a.user_id = u.id
				  WHERE cm.competition_id = :id),
				scored AS (
				  SELECT m.user_id, m.display_name,
				    COALESCE((SELECT s.equity FROM account_snapshots s WHERE s.account_id = m.account_id
				              AND s.day < :start ORDER BY s.day DESC LIMIT 1),
				             (SELECT SUM(l.cash_amount) FROM ledger_entries l
				              WHERE l.account_id = m.account_id AND l.type = 'DEPOSIT')) AS baseline,
				    (SELECT s.equity FROM account_snapshots s WHERE s.account_id = m.account_id
				       AND s.day BETWEEN :start AND :end ORDER BY s.day DESC LIMIT 1) AS final_equity
				  FROM m)
				SELECT user_id, display_name,
				  ROUND((final_equity - baseline) * 100 / NULLIF(baseline, 0), 2) AS return_pct,
				  RANK() OVER (ORDER BY (final_equity - baseline) / NULLIF(baseline, 0) DESC NULLS LAST) AS rnk
				FROM scored ORDER BY rnk, display_name
				""")
			.param("id", competitionId)
			.param("start", Date.valueOf(start))
			.param("end", Date.valueOf(end))
			.query((rs, n) -> new StandingRow(rs.getInt("rnk"), rs.getLong("user_id"), rs.getString("display_name"),
					rs.getBigDecimal("return_pct")))
			.list();
	}
}
