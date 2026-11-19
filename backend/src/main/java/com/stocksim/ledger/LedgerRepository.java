package com.stocksim.ledger;

import java.math.BigDecimal;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Append-only writes. There is deliberately no update or delete. */
@Repository
public class LedgerRepository {

	private final JdbcClient jdbc;

	LedgerRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	public void append(long accountId, LedgerEntryType type, String symbol, BigDecimal cashAmount,
			BigDecimal qtyAmount, Long orderId) {
		jdbc.sql("""
				INSERT INTO ledger_entries (account_id, type, symbol, cash_amount, qty_amount, order_id)
				VALUES (?, ?, ?, ?, ?, ?)
				""").params(accountId, type.name(), symbol, cashAmount, qtyAmount, orderId).update();
	}
}
