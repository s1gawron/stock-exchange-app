package com.stocksim.common;

import org.springframework.http.HttpStatus;

/** Business error that maps directly to an HTTP status with a safe, user-facing message. */
public class ApiException extends RuntimeException {

	private final HttpStatus status;

	public ApiException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus status() {
		return status;
	}
}
