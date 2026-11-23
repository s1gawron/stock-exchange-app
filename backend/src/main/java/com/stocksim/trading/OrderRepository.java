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

	/** Fields the executor needs about an order. */
	record OrderRow(long id, long accountId, String symbol, OrderSide side, OrderType type, BigDecimal qty,
			BigDecimal limitPrice, BigDecimal stopPrice, OrderStatus status, BigDecimal reservedCash,
			BigDecimal reservedQty) {
	}

	/** Data for a new order row. */
	record NewOrder(long accountId, String symbol, OrderSide side, OrderType type, BigDecimal qty,
			BigDecimal limitPrice, BigDecimal stopPrice, TimeInForce timeInForce, OrderStatus status,
			RejectReason rejectReason, BigDecimal reservedCash, BigDecimal reservedQty, Instant expiresAt,
			String idempotencyKey) {
	}

	private static final String VIEW_SQL = """
			SELECT o.id, o.symbol, o.side, o.type, o.qty, o.limit_price, o.stop_price, o.time_in_force, o.status,
			       o.reject_reason, o.reserved_cash, o.reserved_qty, o.expires_at, o.created_at, o.updated_at,
			       f.price, f.gross_amount, f.fee, f.realized_pnl, f.quote_time, f.executed_at
			FROM orders o LEFT JOIN fills f ON f.order_id = o.id
			""";

	private static final String ROW_COLUMNS = """
			id, account_id, symbol, side, type, qty, limit_price, stop_price, status, reserved_cash, reserved_qty
			""";

	private final JdbcClient jdbc;

	OrderRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	Optional<OrderView> findByIdempotencyKey(long accountId, String key) {
		return jdbc.sql(VIEW_SQL + " WHERE o.account_id = ? AND o.idempotency_key = ?")
			.params(accountId, key)
			.query(OrderRepository::mapView)
			.optional();
	}

	OrderView get(long orderId) {
		return jdbc.sql(VIEW_SQL + " WHERE o.id = ?").param(orderId).query(OrderRepository::mapView).single();
	}

	List<OrderView> list(long accountId, OrderStatus status, int limit, int offset) {
		String where = status == null ? " WHERE o.account_id = ?" : " WHERE o.account_id = ? AND o.status = ?";
		var spec = jdbc.sql(VIEW_SQL + where + " ORDER BY o.created_at DESC, o.id DESC LIMIT ? OFFSET ?");
		return (status == null ? spec.params(accountId, limit, offset)
				: spec.params(accountId, status.name(), limit, offset))
			.query(OrderRepository::mapView)
			.list();
	}

	/** Locks the order row. Callers must already hold the account lock. */
	Optional<OrderRow> lockRow(long orderId) {
		return jdbc.sql("SELECT " + ROW_COLUMNS + " FROM orders WHERE id = ? FOR UPDATE")
			.param(orderId)
			.query(OrderRepository::mapRow)
			.optional();
	}

	Optional<Long> accountIdOf(long orderId) {
		return jdbc.sql("SELECT account_id FROM orders WHERE id = ?").param(orderId).query(Long.class).optional();
	}

	/** OPEN orders for one symbol, oldest first (first come, first served). */
	List<OrderRow> openForSymbol(String symbol) {
		return jdbc.sql("SELECT " + ROW_COLUMNS + " FROM orders WHERE status = 'OPEN' AND symbol = ? ORDER BY id")
			.param(symbol)
			.query(OrderRepository::mapRow)
			.list();
	}

	List<String> openSymbols() {
		return jdbc.sql("SELECT DISTINCT symbol FROM orders WHERE status = 'OPEN' ORDER BY symbol")
			.query(String.class)
			.list();
	}

	List<Long> dueForExpiry(Instant now) {
		return jdbc.sql("SELECT id FROM orders WHERE status = 'OPEN' AND expires_at <= ? ORDER BY id")
			.param(Timestamp.from(now))
			.query(Long.class)
			.list();
	}

	long insert(NewOrder o) {
		return jdbc.sql("""
				INSERT INTO orders (account_id, symbol, side, type, qty, limit_price, stop_price, time_in_force, status,
				  reject_reason, reserved_cash, reserved_qty, expires_at, idempotency_key)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
				""")
			.params(o.accountId(), o.symbol(), o.side().name(), o.type().name(), o.qty(), o.limitPrice(),
					o.stopPrice(), o.timeInForce() == null ? null : o.timeInForce().name(), o.status().name(),
					o.rejectReason() == null ? null : o.rejectReason().name(), o.reservedCash(), o.reservedQty(),
					o.expiresAt() == null ? null : Timestamp.from(o.expiresAt()), o.idempotencyKey())
			.query(Long.class)
			.single();
	}

	/** Sum of realized P&L over all fills of the account's orders. */
	BigDecimal realizedPnl(long accountId) {
		return jdbc.sql("""
				SELECT COALESCE(SUM(f.realized_pnl), 0) FROM fills f JOIN orders o ON o.id = f.order_id
				WHERE o.account_id = ?
				""").param(accountId).query(BigDecimal.class).single();
	}

	/** Moves an order out of OPEN and clears its reservation fields. */
	void close(long orderId, OrderStatus status, RejectReason reason) {
		jdbc.sql("""
				UPDATE orders SET status = ?, reject_reason = ?, reserved_cash = 0, reserved_qty = 0, updated_at = now()
				WHERE id = ?
				""").params(status.name(), reason == null ? null : reason.name(), orderId).update();
	}

	void insertFill(long orderId, BigDecimal qty, BigDecimal price, BigDecimal gross, BigDecimal fee,
			BigDecimal realizedPnl, Instant quoteTime) {
		jdbc.sql("""
				INSERT INTO fills (order_id, qty, price, gross_amount, fee, realized_pnl, quote_time)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""").params(orderId, qty, price, gross, fee, realizedPnl, Timestamp.from(quoteTime)).update();
	}

	private static OrderRow mapRow(ResultSet rs, int row) throws SQLException {
		return new OrderRow(rs.getLong("id"), rs.getLong("account_id"), rs.getString("symbol"),
				OrderSide.valueOf(rs.getString("side")), OrderType.valueOf(rs.getString("type")), rs.getBigDecimal("qty"),
				rs.getBigDecimal("limit_price"), rs.getBigDecimal("stop_price"),
				OrderStatus.valueOf(rs.getString("status")), rs.getBigDecimal("reserved_cash"),
				rs.getBigDecimal("reserved_qty"));
	}

	private static OrderView mapView(ResultSet rs, int row) throws SQLException {
		String reason = rs.getString("reject_reason");
		String tif = rs.getString("time_in_force");
		Timestamp expires = rs.getTimestamp("expires_at");
		BigDecimal price = rs.getBigDecimal("price");
		OrderView.Fill fill = price == null ? null
				: new OrderView.Fill(price, rs.getBigDecimal("gross_amount"), rs.getBigDecimal("fee"),
						rs.getBigDecimal("realized_pnl"), rs.getTimestamp("quote_time").toInstant(),
						rs.getTimestamp("executed_at").toInstant());
		return new OrderView(rs.getLong("id"), rs.getString("symbol"), OrderSide.valueOf(rs.getString("side")),
				OrderType.valueOf(rs.getString("type")), rs.getBigDecimal("qty"), rs.getBigDecimal("limit_price"),
				rs.getBigDecimal("stop_price"), tif == null ? null : TimeInForce.valueOf(tif),
				OrderStatus.valueOf(rs.getString("status")), reason == null ? null : RejectReason.valueOf(reason),
				rs.getBigDecimal("reserved_cash"), rs.getBigDecimal("reserved_qty"),
				expires == null ? null : expires.toInstant(), rs.getTimestamp("created_at").toInstant(),
				rs.getTimestamp("updated_at").toInstant(), fill);
	}
}
