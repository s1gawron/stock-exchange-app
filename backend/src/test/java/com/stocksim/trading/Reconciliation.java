package com.stocksim.trading;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.simple.JdbcClient;

/** Core invariants: cached cash/positions equal the ledger; reservations equal what OPEN orders hold. */
final class Reconciliation {

	private Reconciliation() {
	}

	static void assertReconciles(JdbcClient jdbc, long accountId) {
		BigDecimal ledgerCash = jdbc.sql("SELECT SUM(cash_amount) FROM ledger_entries WHERE account_id = ?")
			.param(accountId)
			.query(BigDecimal.class)
			.single();
		BigDecimal cash = jdbc.sql("SELECT cash_balance FROM accounts WHERE id = ?")
			.param(accountId)
			.query(BigDecimal.class)
			.single();
		assertThat(cash).as("cash vs ledger").isEqualByComparingTo(ledgerCash);

		Map<String, BigDecimal> ledgerQty = new HashMap<>();
		jdbc.sql("""
				SELECT symbol, SUM(qty_amount) FROM ledger_entries
				WHERE account_id = ? AND symbol IS NOT NULL GROUP BY symbol HAVING SUM(qty_amount) <> 0
				""").param(accountId).query((rs, n) -> ledgerQty.put(rs.getString(1), rs.getBigDecimal(2))).list();
		Map<String, BigDecimal> positionQty = new HashMap<>();
		jdbc.sql("SELECT symbol, qty FROM positions WHERE account_id = ?")
			.param(accountId)
			.query((rs, n) -> positionQty.put(rs.getString(1), rs.getBigDecimal(2)))
			.list();
		assertThat(positionQty.keySet()).as("position symbols vs ledger").isEqualTo(ledgerQty.keySet());
		positionQty.forEach((s, q) -> assertThat(q).as("qty of " + s).isEqualByComparingTo(ledgerQty.get(s)));

		BigDecimal reservedCash = jdbc.sql("SELECT reserved_cash FROM accounts WHERE id = ?")
			.param(accountId)
			.query(BigDecimal.class)
			.single();
		BigDecimal openReservedCash = jdbc
			.sql("SELECT COALESCE(SUM(reserved_cash), 0) FROM orders WHERE account_id = ? AND status = 'OPEN'")
			.param(accountId)
			.query(BigDecimal.class)
			.single();
		assertThat(reservedCash).as("reserved cash vs open orders").isEqualByComparingTo(openReservedCash);

		Integer badReservedQty = jdbc.sql("""
				SELECT count(*) FROM positions p WHERE p.account_id = ? AND p.reserved_qty <> (
				  SELECT COALESCE(SUM(o.reserved_qty), 0) FROM orders o
				  WHERE o.account_id = p.account_id AND o.symbol = p.symbol AND o.status = 'OPEN')
				""").param(accountId).query(Integer.class).single();
		assertThat(badReservedQty).as("positions whose reserved qty differs from open orders").isZero();
		Integer closedWithReservation = jdbc.sql("""
				SELECT count(*) FROM orders WHERE account_id = ? AND status <> 'OPEN'
				  AND (reserved_cash <> 0 OR reserved_qty <> 0)
				""").param(accountId).query(Integer.class).single();
		assertThat(closedWithReservation).as("closed orders still holding a reservation").isZero();
	}
}
