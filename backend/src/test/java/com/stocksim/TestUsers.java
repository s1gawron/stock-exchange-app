package com.stocksim;

import java.util.UUID;

import com.stocksim.account.AccountService;

import org.springframework.jdbc.core.simple.JdbcClient;

/** Creates a user with a funded account directly in the DB. */
public final class TestUsers {

	private TestUsers() {
	}

	public static long create(JdbcClient jdbc, AccountService accounts, String displayName) {
		long id = jdbc.sql("INSERT INTO users (email, display_name, password_hash) VALUES (?, ?, 'x') RETURNING id")
			.params(UUID.randomUUID() + "@test.local", displayName)
			.query(Long.class)
			.single();
		accounts.openAccount(id);
		return id;
	}
}
