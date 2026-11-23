package com.stocksim.competition;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Owns the {@code competitions} and {@code competition_members} tables. */
@Repository
class CompetitionRepository {

	/** Row of {@code competitions} plus its member count. Stays inside this module. */
	record CompetitionRow(long id, String name, String code, LocalDate startsOn, LocalDate endsOn,
			long creatorUserId, int members) {
	}

	private final JdbcClient jdbc;

	CompetitionRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	int countCreatedEndingOnOrAfter(long userId, LocalDate day) {
		return jdbc.sql("SELECT count(*) FROM competitions WHERE creator_user_id = ? AND ends_on >= ?")
			.params(userId, Date.valueOf(day))
			.query(Integer.class)
			.single();
	}

	/** Throws DuplicateKeyException when the code is already taken. */
	long insert(String name, String code, long creatorUserId, LocalDate startsOn, LocalDate endsOn) {
		return jdbc.sql("""
				INSERT INTO competitions (name, code, creator_user_id, starts_on, ends_on)
				VALUES (?, ?, ?, ?, ?) RETURNING id
				""").params(name, code, creatorUserId, Date.valueOf(startsOn), Date.valueOf(endsOn))
			.query(Long.class)
			.single();
	}

	void addMember(long competitionId, long userId) {
		jdbc.sql("INSERT INTO competition_members (competition_id, user_id) VALUES (?, ?)")
			.params(competitionId, userId)
			.update();
	}

	void addMemberIfAbsent(long competitionId, long userId) {
		jdbc.sql("INSERT INTO competition_members (competition_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING")
			.params(competitionId, userId)
			.update();
	}

	void removeMember(long competitionId, long userId) {
		jdbc.sql("DELETE FROM competition_members WHERE competition_id = ? AND user_id = ?")
			.params(competitionId, userId)
			.update();
	}

	void delete(long competitionId) {
		jdbc.sql("DELETE FROM competitions WHERE id = ?").param(competitionId).update();
	}

	/** Locks the competition row with this code until the transaction ends. */
	Optional<Long> lockIdByCode(String code) {
		return jdbc.sql("SELECT id FROM competitions WHERE code = ? FOR UPDATE")
			.param(code)
			.query(Long.class)
			.optional();
	}

	boolean isMember(long competitionId, long userId) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM competition_members WHERE competition_id = ? AND user_id = ?)")
			.params(competitionId, userId)
			.query(Boolean.class)
			.single();
	}

	/** Ids of the competitions the user is in, latest end first. */
	List<Long> idsOfMember(long userId) {
		return jdbc.sql("""
				SELECT c.id FROM competitions c JOIN competition_members m ON m.competition_id = c.id
				WHERE m.user_id = ? ORDER BY c.ends_on DESC, c.id DESC
				""").param(userId).query(Long.class).list();
	}

	Optional<CompetitionRow> find(long id) {
		return jdbc.sql("""
				SELECT c.id, c.name, c.code, c.starts_on, c.ends_on, c.creator_user_id,
				       (SELECT count(*) FROM competition_members m WHERE m.competition_id = c.id) AS members
				FROM competitions c WHERE c.id = ?
				""").param(id)
			.query((rs, n) -> new CompetitionRow(rs.getLong("id"), rs.getString("name"), rs.getString("code"),
					rs.getDate("starts_on").toLocalDate(), rs.getDate("ends_on").toLocalDate(),
					rs.getLong("creator_user_id"), rs.getInt("members")))
			.optional();
	}
}
