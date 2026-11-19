package com.stocksim.account;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

	private final JdbcClient jdbc;

	AccountRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public long create(long userId, BigDecimal cash) {
		return jdbc.sql("INSERT INTO accounts (user_id, cash_balance) VALUES (?, ?) RETURNING id")
			.params(userId, cash)
			.query(Long.class)
			.single();
	}

	public Optional<Account> findByUserId(long userId) {
		return jdbc.sql("SELECT id, user_id, currency, cash_balance FROM accounts WHERE user_id = ?")
			.param(userId)
			.query((rs, n) -> new Account(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBigDecimal(4)))
			.optional();
	}

	/** Locks the account row until the transaction ends. Every cash or position change must call this first. */
	public Account lock(long accountId) {
		return jdbc.sql("SELECT id, user_id, currency, cash_balance FROM accounts WHERE id = ? FOR UPDATE")
			.param(accountId)
			.query((rs, n) -> new Account(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBigDecimal(4)))
			.single();
	}

	public void setCash(long accountId, BigDecimal cash) {
		jdbc.sql("UPDATE accounts SET cash_balance = ? WHERE id = ?").params(cash, accountId).update();
	}
}
