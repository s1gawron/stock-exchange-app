package com.stocksim.portfolio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Public API of the portfolio module for positions. Write methods join the caller's transaction (no own
 * {@code @Transactional}); callers must hold the account row lock before writing.
 */
@Service
public class PositionService {

	private final PositionRepository repository;

	PositionService(PositionRepository repository) {
		this.repository = repository;
	}

	public Optional<PositionHolding> find(long accountId, String symbol) {
		return repository.find(accountId, symbol).map(PositionService::toHolding);
	}

	/** Writes the position; a quantity of zero deletes it. */
	public void save(long accountId, String symbol, BigDecimal qty, BigDecimal costBasis, BigDecimal reservedQty) {
		repository.save(accountId, symbol, qty, costBasis, reservedQty);
	}

	public void addReserved(long accountId, String symbol, BigDecimal delta) {
		repository.addReserved(accountId, symbol, delta);
	}

	/** Symbols held by anyone. */
	public List<String> heldSymbols() {
		return repository.heldSymbols();
	}

	/** All positions of all accounts (for the daily snapshot job). */
	public List<PositionHolding> all() {
		return repository.all().stream().map(PositionService::toHolding).toList();
	}

	private static PositionHolding toHolding(PositionRow r) {
		return new PositionHolding(r.accountId(), r.symbol(), r.qty(), r.costBasis(), r.reservedQty());
	}
}
