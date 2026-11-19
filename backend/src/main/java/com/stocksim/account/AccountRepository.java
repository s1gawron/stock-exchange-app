package com.stocksim.account;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

	private static final String COLUMNS = "id, user_id, currency, cash_balance, reserved_cash";

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
		return jdbc.sql("SELECT " + COLUMNS + " FROM accounts WHERE user_id = ?")
			.param(userId)
			.query(AccountRepository::map)
			.optional();
	}

	public long userIdOf(long accountId) {
		return jdbc.sql("SELECT user_id FROM accounts WHERE id = ?").param(accountId).query(Long.class).single();
	}

	/** Locks the account row until the transaction ends. Every cash, reservation or position change calls this first. */
	public Account lock(long accountId) {
		return jdbc.sql("SELECT " + COLUMNS + " FROM accounts WHERE id = ? FOR UPDATE")
			.param(accountId)
			.query(AccountRepository::map)
			.single();
	}

	public void update(long accountId, BigDecimal cash, BigDecimal reservedCash) {
		jdbc.sql("UPDATE accounts SET cash_balance = ?, reserved_cash = ? WHERE id = ?")
			.params(cash, reservedCash, accountId)
			.update();
	}

	private static Account map(ResultSet rs, int row) throws SQLException {
		return new Account(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBigDecimal(4), rs.getBigDecimal(5));
	}
}
