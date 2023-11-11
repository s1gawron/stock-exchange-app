package com.s1gawron.stockexchange.user.exception;

public class UserEmailExistsException extends RuntimeException {

    private UserEmailExistsException(final String message) {
        super(message);
    }

    public static UserEmailExistsException create() {
        return new UserEmailExistsException("Account with given email already exists!");
    }
}
