package com.stocksim.auth;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class UserRepository {

	private final JdbcClient jdbc;

	UserRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	Optional<AppUser> findByEmail(String email) {
		return jdbc.sql("SELECT id, email, display_name, password_hash FROM users WHERE lower(email) = lower(?)")
			.param(email)
			.query((rs, n) -> new AppUser(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)))
			.optional();
	}

	boolean emailExists(String email) {
		return jdbc.sql("SELECT EXISTS (SELECT 1 FROM users WHERE lower(email) = lower(?))")
			.param(email)
			.query(Boolean.class)
			.single();
	}

	long insert(String email, String displayName, String passwordHash) {
		return jdbc.sql("INSERT INTO users (email, display_name, password_hash) VALUES (?, ?, ?) RETURNING id")
			.params(email, displayName, passwordHash)
			.query(Long.class)
			.single();
	}
}
