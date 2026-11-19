package com.stocksim.competition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

import com.stocksim.IntegrationTest;
import com.stocksim.TestUsers;
import com.stocksim.account.AccountService;
import com.stocksim.common.ApiException;
import com.stocksim.performance.SnapshotService;
import com.stocksim.trading.OrderSide;
import com.stocksim.trading.TradingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

class CompetitionIntegrationTest extends IntegrationTest {

	@Autowired
	CompetitionService competitions;

	@Autowired
	SnapshotService snapshots;

	@Autowired
	TradingService trading;

	@Autowired
	AccountService accounts;

	@Autowired
	JdbcClient jdbc;

	private static final LocalDate START = LocalDate.of(2200, 3, 1);
	private static final LocalDate END = LocalDate.of(2200, 3, 31);

	@Test
	void createJoinAndScoreFromSnapshots() {
		long alice = TestUsers.create(jdbc, accounts, "Alice");
		long bob = TestUsers.create(jdbc, accounts, "Bob");
		long carol = TestUsers.create(jdbc, accounts, "Carol");
		var c = competitions.create(alice, "  Spring cup ", START, END);
		assertThat(c.name()).isEqualTo("Spring cup");
		assertThat(c.code()).hasSize(8);
		assertThat(c.status()).isEqualTo(CompetitionService.Status.UPCOMING);
		assertThat(c.creator()).isTrue();

		competitions.join(bob, c.code().toLowerCase());
		assertThat(competitions.join(bob, c.code()).members()).isEqualTo(2); // joining twice is harmless
		assertThatThrownBy(() -> competitions.details(carol, c.id())).isInstanceOf(ApiException.class)
			.hasMessageContaining("not found");

		// Alice gained BEFORE the start: must not count. Bob gains during the competition.
		setPrice("AAPL", "100");
		buy(alice, 500);
		buy(bob, 500);
		setPrice("AAPL", "120");
		snapshots.snapshot(START.minusDays(1)); // baseline: both 110000
		setPrice("AAPL", "132");
		snapshots.snapshot(START.plusDays(5)); // both 116000 -> +5.45%
		sellAll(alice);                         // Alice locks in 116000
		setPrice("AAPL", "110");
		snapshots.snapshot(END);                // Alice 116000 (+5.45%), Bob 105000 (-4.55%)
		setPrice("AAPL", "500");
		snapshots.snapshot(END.plusDays(1));    // after the end: ignored

		var details = competitions.details(bob, c.id());
		assertThat(details.standings()).hasSize(2);
		var first = details.standings().getFirst();
		assertThat(first.displayName()).isEqualTo("Alice");
		assertThat(first.rank()).isEqualTo(1);
		assertThat(first.returnPercent()).isEqualByComparingTo("5.45");
		var second = details.standings().get(1);
		assertThat(second.displayName()).isEqualTo("Bob");
		assertThat(second.returnPercent()).isEqualByComparingTo("-4.55");
		assertThat(second.me()).isTrue();
	}

	@Test
	void cannotJoinOrLeaveAfterTheStart() {
		long owner = TestUsers.create(jdbc, accounts, "Owner");
		long late = TestUsers.create(jdbc, accounts, "Late");
		var c = competitions.create(owner, "Started", START, END);
		jdbc.sql("UPDATE competitions SET starts_on = ? WHERE id = ?")
			.params(Date.valueOf(LocalDate.of(2020, 1, 1)), c.id()).update();

		assertThatThrownBy(() -> competitions.join(late, c.code())).isInstanceOf(ApiException.class)
			.hasMessageContaining("started");
		assertThatThrownBy(() -> competitions.leave(owner, c.id())).isInstanceOf(ApiException.class);
		assertThat(competitions.mine(owner)).extracting(CompetitionService.Summary::status)
			.contains(CompetitionService.Status.ACTIVE);
	}

	@Test
	void leavingBeforeStartAndCreatorLeavingDeletes() {
		long owner = TestUsers.create(jdbc, accounts, "Owner2");
		long member = TestUsers.create(jdbc, accounts, "Member2");
		var c = competitions.create(owner, "Soon", START, END);
		competitions.join(member, c.code());
		competitions.leave(member, c.id());
		assertThat(competitions.mine(member)).isEmpty();
		competitions.leave(owner, c.id());
		assertThatThrownBy(() -> competitions.join(member, c.code())).isInstanceOf(ApiException.class)
			.hasMessageContaining("No competition");
	}

	@Test
	void validation() {
		long u = TestUsers.create(jdbc, accounts, "V");
		assertThatThrownBy(() -> competitions.create(u, "Past", LocalDate.of(2020, 1, 1), END))
			.isInstanceOf(ApiException.class);
		assertThatThrownBy(() -> competitions.create(u, "Backwards", END, START)).isInstanceOf(ApiException.class);
		assertThatThrownBy(() -> competitions.create(u, "Too long", START, START.plusDays(400)))
			.isInstanceOf(ApiException.class);
		assertThatThrownBy(() -> competitions.join(u, "NOPE1234")).isInstanceOf(ApiException.class);
	}

	private void buy(long user, int qty) {
		trading.placeMarketOrder(user, "AAPL", OrderSide.BUY, BigDecimal.valueOf(qty), UUID.randomUUID().toString());
	}

	private void sellAll(long user) {
		trading.placeMarketOrder(user, "AAPL", OrderSide.SELL, BigDecimal.valueOf(500), UUID.randomUUID().toString());
	}
}
