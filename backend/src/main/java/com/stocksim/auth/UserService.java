package com.stocksim.auth;

import com.stocksim.account.AccountService;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserService implements UserDetailsService {

	private final UserRepository users;
	private final AccountService accounts;
	private final PasswordEncoder passwordEncoder;

	UserService(UserRepository users, AccountService accounts, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.accounts = accounts;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public AppUser loadUserByUsername(String email) {
		return users.findByEmail(email)
			.map(u -> new AppUser(u.id(), u.email(), u.displayName(), u.passwordHash()))
			.orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
	}

	/** Creates the user and opens their account with starting cash in one transaction. */
	@Transactional
	public void register(String email, String displayName, String rawPassword) {
		String normalized = email.trim();
		if (users.emailExists(normalized)) {
			throw new EmailTakenException();
		}
		long userId = users.insert(normalized, displayName.trim(), passwordEncoder.encode(rawPassword));
		accounts.openAccount(userId);
	}

	static class EmailTakenException extends RuntimeException {

		EmailTakenException() {
			super("Email is already registered");
		}
	}
}
