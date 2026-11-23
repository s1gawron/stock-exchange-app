package com.stocksim.watchlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stocksim.IntegrationTest;
import com.stocksim.TestUsers;
import com.stocksim.account.AccountService;
import com.stocksim.common.ApiException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Guards the watchlist rules: unknown symbol, size limit, order, names, popularity. */
class WatchlistServiceTest extends IntegrationTest {

	@Autowired
	WatchlistService watchlist;

	@Autowired
	AccountService accounts;

	@Autowired
	JdbcClient jdbc;

	@Test
	void addListRemoveKeepsOrderAndUppercasesSymbols() {
		long user = TestUsers.create(jdbc, accounts, "W1");
		watchlist.add(user, "msft");
		watchlist.add(user, "AAPL");
		watchlist.add(user, "aapl"); // duplicate is ignored

		var items = watchlist.list(user);
		assertThat(items).extracting(WatchlistService.Item::symbol).containsExactly("MSFT", "AAPL");
		assertThat(items.getFirst().name()).isNotBlank();
		assertThat(items.getFirst().price()).isNotNull(); // fake provider has quotes

		watchlist.remove(user, "msft");
		assertThat(watchlist.list(user)).extracting(WatchlistService.Item::symbol).containsExactly("AAPL");
	}

	@Test
	void unknownSymbolIsRejectedWith404() {
		long user = TestUsers.create(jdbc, accounts, "W2");
		assertThatThrownBy(() -> watchlist.add(user, "NOPE"))
			.isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.status()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	@Test
	void fullWatchlistIsRejectedWith422() {
		long user = TestUsers.create(jdbc, accounts, "W3");
		for (int i = 0; i < 50; i++) {
			String symbol = "WL" + i;
			jdbc.sql("INSERT INTO instruments (symbol, display_symbol, name, type) VALUES (?, ?, ?, 'Common Stock') "
					+ "ON CONFLICT (symbol) DO NOTHING").params(symbol, symbol, "Watch " + i).update();
			watchlist.add(user, symbol);
		}
		assertThatThrownBy(() -> watchlist.add(user, "AAPL")).isInstanceOfSatisfying(ApiException.class,
				e -> assertThat(e.status()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT));
	}

	@Test
	void popularSymbolsComeFirst() {
		long a = TestUsers.create(jdbc, accounts, "W4");
		long b = TestUsers.create(jdbc, accounts, "W5");
		watchlist.add(a, "TSLA");
		watchlist.add(a, "NVDA");
		watchlist.add(b, "NVDA");
		assertThat(watchlist.symbolsByPopularity()).contains("NVDA", "TSLA");
		assertThat(watchlist.symbolsByPopularity().indexOf("NVDA"))
			.isLessThan(watchlist.symbolsByPopularity().indexOf("TSLA"));
	}
}
