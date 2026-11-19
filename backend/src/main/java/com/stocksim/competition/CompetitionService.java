package com.stocksim.competition;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.stocksim.common.ApiException;
import com.stocksim.marketdata.UsMarketCalendar;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Competitions rank members' normal accounts by % return over [startsOn, endsOn], from daily snapshots:
 * baseline = equity at the last close before startsOn (or the deposits for accounts opened later),
 * end = equity at the last snapshot up to endsOn. Joining is possible until the start day's open.
 */
@Service
public class CompetitionService {

	public enum Status {
		UPCOMING, ACTIVE, FINISHED
	}

	public record Summary(long id, String name, String code, LocalDate startsOn, LocalDate endsOn, Status status,
			int members, boolean creator) {
	}

	/** @param returnPercent null until the first snapshot inside the competition */
	public record Standing(int rank, String displayName, BigDecimal returnPercent, boolean me) {
	}

	public record Details(Summary competition, LocalDate asOf, List<Standing> standings) {
	}

	static final int MAX_MEMBERS = 200;
	static final int MAX_OPEN_CREATED = 10;
	static final int MAX_DAYS = 366;
	private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
	private static final LocalTime OPEN = LocalTime.of(9, 30);

	private final JdbcClient jdbc;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	CompetitionService(JdbcClient jdbc, Clock clock) {
		this.jdbc = jdbc;
		this.clock = clock;
	}

	@Transactional
	public Summary create(long userId, String name, LocalDate startsOn, LocalDate endsOn) {
		if (!joinable(startsOn)) {
			throw bad("Start must be a future day (or today before the US open)");
		}
		if (endsOn.isBefore(startsOn)) {
			throw bad("End must be on or after the start");
		}
		if (ChronoUnit.DAYS.between(startsOn, endsOn) >= MAX_DAYS) {
			throw bad("A competition can last at most " + MAX_DAYS + " days");
		}
		int open = jdbc.sql("SELECT count(*) FROM competitions WHERE creator_user_id = ? AND ends_on >= ?")
			.params(userId, Date.valueOf(today()))
			.query(Integer.class)
			.single();
		if (open >= MAX_OPEN_CREATED) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
					"You can have at most " + MAX_OPEN_CREATED + " unfinished competitions");
		}
		long id = insertWithUniqueCode(userId, name.trim(), startsOn, endsOn);
		jdbc.sql("INSERT INTO competition_members (competition_id, user_id) VALUES (?, ?)").params(id, userId).update();
		return summary(id, userId).orElseThrow();
	}

	@Transactional
	public Summary join(long userId, String code) {
		long id = jdbc.sql("SELECT id FROM competitions WHERE code = ? FOR UPDATE")
			.param(code.trim().toUpperCase(Locale.ROOT))
			.query(Long.class)
			.optional()
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No competition with this code"));
		Summary c = summary(id, userId).orElseThrow();
		if (!joinable(c.startsOn())) {
			throw new ApiException(HttpStatus.CONFLICT, "This competition has already started");
		}
		if (c.members() >= MAX_MEMBERS) {
			throw new ApiException(HttpStatus.CONFLICT, "This competition is full");
		}
		jdbc.sql("INSERT INTO competition_members (competition_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING")
			.params(id, userId)
			.update();
		return summary(id, userId).orElseThrow();
	}

	@Transactional
	public void leave(long userId, long competitionId) {
		Summary c = memberSummary(userId, competitionId);
		if (!joinable(c.startsOn())) {
			throw new ApiException(HttpStatus.CONFLICT, "You cannot leave after the start");
		}
		if (c.creator()) {
			// Creator leaving an upcoming competition removes it for everyone.
			jdbc.sql("DELETE FROM competitions WHERE id = ?").param(competitionId).update();
			return;
		}
		jdbc.sql("DELETE FROM competition_members WHERE competition_id = ? AND user_id = ?")
			.params(competitionId, userId)
			.update();
	}

	public List<Summary> mine(long userId) {
		return jdbc.sql("""
				SELECT c.id FROM competitions c JOIN competition_members m ON m.competition_id = c.id
				WHERE m.user_id = ? ORDER BY c.ends_on DESC, c.id DESC
				""").param(userId).query(Long.class).list().stream()
			.map(id -> summary(id, userId).orElseThrow())
			.toList();
	}

	/** Standings, visible to members only. */
	public Details details(long userId, long competitionId) {
		Summary c = memberSummary(userId, competitionId);
		Optional<LocalDate> asOf = c.status() == Status.UPCOMING ? Optional.empty()
				: jdbc.sql("SELECT max(day) FROM account_snapshots WHERE day BETWEEN ? AND ?")
					.params(Date.valueOf(c.startsOn()), Date.valueOf(c.endsOn()))
					.query(Date.class)
					.optional()
					.map(Date::toLocalDate);
		List<Standing> standings = jdbc.sql("""
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
			.param("start", Date.valueOf(c.startsOn()))
			.param("end", Date.valueOf(c.endsOn()))
			.query((rs, n) -> new Standing(rs.getInt("rnk"), rs.getString("display_name"),
					rs.getBigDecimal("return_pct"), rs.getLong("user_id") == userId))
			.list();
		return new Details(c, asOf.orElse(null), standings);
	}

	private Summary memberSummary(long userId, long competitionId) {
		boolean member = jdbc.sql("SELECT EXISTS (SELECT 1 FROM competition_members WHERE competition_id = ? AND user_id = ?)")
			.params(competitionId, userId)
			.query(Boolean.class)
			.single();
		if (!member) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Competition not found");
		}
		return summary(competitionId, userId).orElseThrow();
	}

	private Optional<Summary> summary(long id, long userId) {
		return jdbc.sql("""
				SELECT c.id, c.name, c.code, c.starts_on, c.ends_on, c.creator_user_id,
				       (SELECT count(*) FROM competition_members m WHERE m.competition_id = c.id) AS members
				FROM competitions c WHERE c.id = ?
				""").param(id).query((rs, n) -> {
			LocalDate start = rs.getDate("starts_on").toLocalDate();
			LocalDate end = rs.getDate("ends_on").toLocalDate();
			return new Summary(rs.getLong("id"), rs.getString("name"), rs.getString("code"), start, end,
					status(start, end), rs.getInt("members"), rs.getLong("creator_user_id") == userId);
		}).optional();
	}

	private Status status(LocalDate start, LocalDate end) {
		LocalDate today = today();
		if (joinable(start)) {
			return Status.UPCOMING;
		}
		return today.isAfter(end) ? Status.FINISHED : Status.ACTIVE;
	}

	/** True until the US open of the start day. */
	private boolean joinable(LocalDate startsOn) {
		return clock.instant().isBefore(startsOn.atTime(OPEN).atZone(UsMarketCalendar.NEW_YORK).toInstant());
	}

	private LocalDate today() {
		return clock.instant().atZone(UsMarketCalendar.NEW_YORK).toLocalDate();
	}

	private long insertWithUniqueCode(long userId, String name, LocalDate startsOn, LocalDate endsOn) {
		for (int attempt = 0; attempt < 5; attempt++) {
			try {
				return jdbc.sql("""
						INSERT INTO competitions (name, code, creator_user_id, starts_on, ends_on)
						VALUES (?, ?, ?, ?, ?) RETURNING id
						""").params(name, newCode(), userId, Date.valueOf(startsOn), Date.valueOf(endsOn))
					.query(Long.class)
					.single();
			}
			catch (DuplicateKeyException ex) {
				// code collision: try another
			}
		}
		throw new IllegalStateException("Could not generate a unique competition code");
	}

	private String newCode() {
		StringBuilder sb = new StringBuilder(8);
		for (int i = 0; i < 8; i++) {
			sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
		}
		return sb.toString();
	}

	private static ApiException bad(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}
}
