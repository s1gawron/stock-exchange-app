package com.stocksim.watchlist;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Owns the {@code watchlist_items} table. */
@Repository
class WatchlistRepository {

	private final JdbcClient jdbc;

	WatchlistRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	/** The user's symbols, oldest first. */
	List<String> symbolsOf(long userId) {
		return jdbc.sql("SELECT symbol FROM watchlist_items WHERE user_id = ? ORDER BY created_at")
			.param(userId)
			.query(String.class)
			.list();
	}

	int count(long userId) {
		return jdbc.sql("SELECT count(*) FROM watchlist_items WHERE user_id = ?")
			.param(userId)
			.query(Integer.class)
			.single();
	}

	void add(long userId, String symbol) {
		jdbc.sql("INSERT INTO watchlist_items (user_id, symbol) VALUES (?, ?) ON CONFLICT DO NOTHING")
			.params(userId, symbol)
			.update();
	}

	void remove(long userId, String symbol) {
		jdbc.sql("DELETE FROM watchlist_items WHERE user_id = ? AND symbol = ?").params(userId, symbol).update();
	}

	/** Symbols on any watchlist, most watched first. */
	List<String> symbolsByPopularity() {
		return jdbc.sql("""
				SELECT symbol FROM watchlist_items GROUP BY symbol ORDER BY count(*) DESC, symbol
				""").query(String.class).list();
	}
}
