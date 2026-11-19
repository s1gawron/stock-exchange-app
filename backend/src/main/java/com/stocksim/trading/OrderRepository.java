package com.stocksim.trading;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class OrderRepository {

	private static final String VIEW_SQL = """
			SELECT o.id, o.symbol, o.side, o.type, o.qty, o.status, o.reject_reason, o.created_at,
			       f.price, f.gross_amount, f.fee, f.realized_pnl, f.quote_time, f.executed_at
			FROM orders o LEFT JOIN fills f ON f.order_id = o.id
			""";

	private final JdbcClient jdbc;

	OrderRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	Optional<OrderView> findByIdempotencyKey(long accountId, String key) {
		return jdbc.sql(VIEW_SQL + " WHERE o.account_id = ? AND o.idempotency_key = ?")
			.params(accountId, key)
			.query(OrderRepository::map)
			.optional();
	}

	OrderView get(long orderId) {
		return jdbc.sql(VIEW_SQL + " WHERE o.id = ?").param(orderId).query(OrderRepository::map).single();
	}

	List<OrderView> list(long accountId, int limit, int offset) {
		return jdbc.sql(VIEW_SQL + " WHERE o.account_id = ? ORDER BY o.created_at DESC, o.id DESC LIMIT ? OFFSET ?")
			.params(accountId, limit, offset)
			.query(OrderRepository::map)
			.list();
	}

	long insert(long accountId, String symbol, OrderSide side, BigDecimal qty, String status, RejectReason reason,
			String idempotencyKey) {
		return jdbc.sql("""
				INSERT INTO orders (account_id, symbol, side, type, qty, status, reject_reason, idempotency_key)
				VALUES (?, ?, ?, 'MARKET', ?, ?, ?, ?) RETURNING id
				""")
			.params(accountId, symbol, side.name(), qty, status, reason == null ? null : reason.name(), idempotencyKey)
			.query(Long.class)
			.single();
	}

	void insertFill(long orderId, BigDecimal qty, BigDecimal price, BigDecimal gross, BigDecimal realizedPnl,
			Instant quoteTime) {
		jdbc.sql("""
				INSERT INTO fills (order_id, qty, price, gross_amount, fee, realized_pnl, quote_time)
				VALUES (?, ?, ?, ?, 0, ?, ?)
				""").params(orderId, qty, price, gross, realizedPnl, Timestamp.from(quoteTime)).update();
	}

	private static OrderView map(ResultSet rs, int row) throws SQLException {
		String reason = rs.getString("reject_reason");
		BigDecimal price = rs.getBigDecimal("price");
		OrderView.Fill fill = price == null ? null
				: new OrderView.Fill(price, rs.getBigDecimal("gross_amount"), rs.getBigDecimal("fee"),
						rs.getBigDecimal("realized_pnl"), rs.getTimestamp("quote_time").toInstant(),
						rs.getTimestamp("executed_at").toInstant());
		return new OrderView(rs.getLong("id"), rs.getString("symbol"), OrderSide.valueOf(rs.getString("side")),
				rs.getString("type"), rs.getBigDecimal("qty"), rs.getString("status"),
				reason == null ? null : RejectReason.valueOf(reason), rs.getTimestamp("created_at").toInstant(), fill);
	}
}
