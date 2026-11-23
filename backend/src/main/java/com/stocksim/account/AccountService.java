package com.stocksim.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.stocksim.ledger.LedgerEntryType;
import com.stocksim.ledger.LedgerService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

	private final AccountRepository accounts;
	private final LedgerService ledger;
	private final BigDecimal startingCash;

	AccountService(AccountRepository accounts, LedgerService ledger,
			@Value("${trading.starting-cash:100000}") BigDecimal startingCash) {
		this.accounts = accounts;
		this.ledger = ledger;
		this.startingCash = startingCash.setScale(2, RoundingMode.UNNECESSARY);
	}

	/** Opens the user's account and records the starting cash as a DEPOSIT ledger entry. */
	@Transactional
	public long openAccount(long userId) {
		long accountId = accounts.create(userId, startingCash);
		ledger.append(accountId, LedgerEntryType.DEPOSIT, null, startingCash, BigDecimal.ZERO, null);
		return accountId;
	}

	public AccountBalance forUser(long userId) {
		return accounts.findByUserId(userId)
			.map(AccountService::toBalance)
			.orElseThrow(() -> new IllegalStateException("No account for user"));
	}

	public long userIdOf(long accountId) {
		return accounts.userIdOf(accountId);
	}

	/** All accounts (for the daily snapshot job). */
	public List<AccountBalance> allBalances() {
		return accounts.listAll().stream().map(AccountService::toBalance).toList();
	}

	/**
	 * Locks the account row until the transaction ends. Every cash, reservation or position change calls this first.
	 * Joins the caller's transaction.
	 */
	public AccountBalance lock(long accountId) {
		return toBalance(accounts.lock(accountId));
	}

	/** Callers must hold the account lock. Joins the caller's transaction. */
	public void updateBalances(long accountId, BigDecimal cash, BigDecimal reservedCash) {
		accounts.update(accountId, cash, reservedCash);
	}

	private static AccountBalance toBalance(AccountRow r) {
		return new AccountBalance(r.id(), r.userId(), r.currency(), r.cashBalance(), r.reservedCash());
	}

	public BigDecimal startingCash() {
		return startingCash;
	}
}
