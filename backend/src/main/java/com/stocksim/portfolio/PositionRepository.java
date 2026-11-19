package com.stocksim.portfolio;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Callers must hold the account row lock (AccountRepository.lock) before writing. */
@Repository
public class PositionRepository {

	private static final String COLUMNS = "account_id, symbol, qty, cost_basis, reserved_qty";

	private final JdbcClient jdbc;

	PositionRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public Optional<Position> find(long accountId, String symbol) {
		return jdbc.sql("SELECT " + COLUMNS + " FROM positions WHERE account_id = ? AND symbol = ?")
			.params(accountId, symbol)
			.query(PositionRepository::map)
			.optional();
	}

	public List<Position> list(long accountId) {
		return jdbc.sql("SELECT " + COLUMNS + " FROM positions WHERE account_id = ? ORDER BY symbol")
			.param(accountId)
			.query(PositionRepository::map)
			.list();
	}

	/** Writes the position; a quantity of zero deletes it. */
	public void save(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis, BigDecimal reservedQty) {
		if (qty.signum() == 0) {
			jdbc.sql("DELETE FROM positions WHERE account_id = ? AND symbol = ?").params(accountId, symbol).update();
			return;
		}
		jdbc.sql("""
				INSERT INTO positions (account_id, symbol, qty, cost_basis, reserved_qty) VALUES (?, ?, ?, ?, ?)
				ON CONFLICT (account_id, symbol) DO UPDATE SET qty = EXCLUDED.qty, cost_basis = EXCLUDED.cost_basis,
				  reserved_qty = EXCLUDED.reserved_qty, updated_at = now()
				""").params(accountId, symbol, qty, costBasis, reservedQty).update();
	}

	public void addReserved(long accountId, String symbol, BigDecimal delta) {
		jdbc.sql("UPDATE positions SET reserved_qty = reserved_qty + ?, updated_at = now() WHERE account_id = ? AND symbol = ?")
			.params(delta, accountId, symbol)
			.update();
	}

	private static Position map(ResultSet rs, int row) throws SQLException {
		return new Position(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3), rs.getBigDecimal(4),
				rs.getBigDecimal(5));
	}
}
