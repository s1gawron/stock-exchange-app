package com.stocksim.competition;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.stocksim.common.ApiException;
import com.stocksim.marketdata.UsMarketCalendar;
import com.stocksim.performance.SnapshotService;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
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

	private final CompetitionRepository competitions;
	private final CompetitionStandingsRepository standingsQuery;
	private final SnapshotService snapshots;
	private final Clock clock;
	private final SecureRandom random = new SecureRandom();

	CompetitionService(CompetitionRepository competitions, CompetitionStandingsRepository standingsQuery,
			SnapshotService snapshots, Clock clock) {
		this.competitions = competitions;
		this.standingsQuery = standingsQuery;
		this.snapshots = snapshots;
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
		int open = competitions.countCreatedEndingOnOrAfter(userId, today());
		if (open >= MAX_OPEN_CREATED) {
			throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT,
					"You can have at most " + MAX_OPEN_CREATED + " unfinished competitions");
		}
		long id = insertWithUniqueCode(userId, name.trim(), startsOn, endsOn);
		competitions.addMember(id, userId);
		return summary(id, userId).orElseThrow();
	}

	@Transactional
	public Summary join(long userId, String code) {
		long id = competitions.lockIdByCode(code.trim().toUpperCase(Locale.ROOT))
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No competition with this code"));
		Summary c = summary(id, userId).orElseThrow();
		if (!joinable(c.startsOn())) {
			throw new ApiException(HttpStatus.CONFLICT, "This competition has already started");
		}
		if (c.members() >= MAX_MEMBERS) {
			throw new ApiException(HttpStatus.CONFLICT, "This competition is full");
		}
		competitions.addMemberIfAbsent(id, userId);
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
			competitions.delete(competitionId);
			return;
		}
		competitions.removeMember(competitionId, userId);
	}

	public List<Summary> mine(long userId) {
		return competitions.idsOfMember(userId).stream()
			.map(id -> summary(id, userId).orElseThrow())
			.toList();
	}

	/** Standings, visible to members only. */
	public Details details(long userId, long competitionId) {
		Summary c = memberSummary(userId, competitionId);
		Optional<LocalDate> asOf = c.status() == Status.UPCOMING ? Optional.empty()
				: snapshots.lastSnapshotDayBetween(c.startsOn(), c.endsOn());
		List<Standing> standings = standingsQuery.standings(competitionId, c.startsOn(), c.endsOn())
			.stream()
			.map(r -> new Standing(r.rank(), r.displayName(), r.returnPercent(), r.userId() == userId))
			.toList();
		return new Details(c, asOf.orElse(null), standings);
	}

	private Summary memberSummary(long userId, long competitionId) {
		boolean member = competitions.isMember(competitionId, userId);
		if (!member) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Competition not found");
		}
		return summary(competitionId, userId).orElseThrow();
	}

	private Optional<Summary> summary(long id, long userId) {
		return competitions.find(id)
			.map(c -> new Summary(c.id(), c.name(), c.code(), c.startsOn(), c.endsOn(),
					status(c.startsOn(), c.endsOn()), c.members(), c.creatorUserId() == userId));
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
				return competitions.insert(name, newCode(), userId, startsOn, endsOn);
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
