package com.stocksim.ledger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Append-only writes. There is deliberately no update or delete. */
@Repository
class LedgerRepository {

	private final JdbcClient jdbc;

	LedgerRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	void append(long accountId, LedgerEntryType type, String symbol, BigDecimal cashAmount,
			BigDecimal qtyAmount, Long orderId) {
		jdbc.sql("""
				INSERT INTO ledger_entries (account_id, type, symbol, cash_amount, qty_amount, order_id)
				VALUES (?, ?, ?, ?, ?, ?)
				""").params(accountId, type.name(), symbol, cashAmount, qtyAmount, orderId).update();
	}

	BigDecimal depositsOf(long accountId) {
		return jdbc
			.sql("SELECT COALESCE(SUM(cash_amount), 0) FROM ledger_entries WHERE account_id = ? AND type = 'DEPOSIT'")
			.param(accountId)
			.query(BigDecimal.class)
			.single();
	}

	Map<Long, BigDecimal> depositsByAccount() {
		Map<Long, BigDecimal> result = new HashMap<>();
		jdbc.sql("SELECT account_id, SUM(cash_amount) FROM ledger_entries WHERE type = 'DEPOSIT' GROUP BY account_id")
			.query((rs, n) -> result.put(rs.getLong(1), rs.getBigDecimal(2)))
			.list();
		return result;
	}
}
