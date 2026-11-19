package com.stocksim.auth;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Authenticated principal stored in the HTTP session (in Redis). The password hash is erased after login. */
public final class AppUser implements UserDetails, CredentialsContainer, Serializable {

	/** Sessions are stored in Redis with Java serialization: keep this stable so stored sessions stay readable. */
	private static final long serialVersionUID = 1L;

	private final long id;
	private final String email;
	private final String displayName;
	private String passwordHash;

	AppUser(long id, String email, String displayName, String passwordHash) {
		this.id = id;
		this.email = email;
		this.displayName = displayName;
		this.passwordHash = passwordHash;
	}

	public long id() {
		return id;
	}

	public String email() {
		return email;
	}

	public String displayName() {
		return displayName;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public void eraseCredentials() {
		passwordHash = null;
	}
}
