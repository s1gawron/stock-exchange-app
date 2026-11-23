package com.stocksim.ledger;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Public API of the ledger module. Writes are append-only. Methods join the caller's transaction (no own
 * {@code @Transactional}), so they stay inside the order executor's locking transaction.
 */
@Service
public class LedgerService {

	private final LedgerRepository repository;

	LedgerService(LedgerRepository repository) {
		this.repository = repository;
	}

	public void append(long accountId, LedgerEntryType type, String symbol, BigDecimal cashAmount,
			BigDecimal qtyAmount, Long orderId) {
		repository.append(accountId, type, symbol, cashAmount, qtyAmount, orderId);
	}

	/** Sum of the account's DEPOSIT entries (zero when there are none). */
	public BigDecimal depositsOf(long accountId) {
		return repository.depositsOf(accountId);
	}

	/** Deposits per account. Accounts without a DEPOSIT entry are missing from the map (treat as zero). */
	public Map<Long, BigDecimal> depositsByAccount() {
		return repository.depositsByAccount();
	}
}
