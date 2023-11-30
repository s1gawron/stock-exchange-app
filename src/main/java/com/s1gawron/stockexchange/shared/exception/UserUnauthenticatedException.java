package com.s1gawron.stockexchange.shared.exception;

public class UserUnauthenticatedException extends RuntimeException {

    private UserUnauthenticatedException(final String message) {
        super(message);
    }

    public static UserUnauthenticatedException create() {
        return new UserUnauthenticatedException("User is not authenticated!");
    }
}
