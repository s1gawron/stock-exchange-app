package com.stocksim.account;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.stocksim.ledger.LedgerEntryType;
import com.stocksim.ledger.LedgerRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

	private final AccountRepository accounts;
	private final LedgerRepository ledger;
	private final BigDecimal startingCash;

	AccountService(AccountRepository accounts, LedgerRepository ledger,
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

	public Account forUser(long userId) {
		return accounts.findByUserId(userId).orElseThrow(() -> new IllegalStateException("No account for user"));
	}

	public BigDecimal startingCash() {
		return startingCash;
	}
}
