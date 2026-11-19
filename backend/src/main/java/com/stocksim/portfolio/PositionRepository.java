package com.stocksim.portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Callers must hold the account row lock (AccountRepository.lock) before writing. */
@Repository
public class PositionRepository {

	private final JdbcClient jdbc;

	PositionRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public Optional<Position> find(long accountId, String symbol) {
		return jdbc.sql("SELECT account_id, symbol, qty, cost_basis FROM positions WHERE account_id = ? AND symbol = ?")
			.params(accountId, symbol)
			.query((rs, n) -> new Position(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3), rs.getBigDecimal(4)))
			.optional();
	}

	public List<Position> list(long accountId) {
		return jdbc.sql("SELECT account_id, symbol, qty, cost_basis FROM positions WHERE account_id = ? ORDER BY symbol")
			.param(accountId)
			.query((rs, n) -> new Position(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3), rs.getBigDecimal(4)))
			.list();
	}

	public void save(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis) {
		if (qty.signum() == 0) {
			jdbc.sql("DELETE FROM positions WHERE account_id = ? AND symbol = ?").params(accountId, symbol).update();
			return;
		}
		jdbc.sql("""
				INSERT INTO positions (account_id, symbol, qty, cost_basis) VALUES (?, ?, ?, ?)
				ON CONFLICT (account_id, symbol)
				DO UPDATE SET qty = EXCLUDED.qty, cost_basis = EXCLUDED.cost_basis, updated_at = now()
				""").params(accountId, symbol, qty, costBasis).update();
	}
}
