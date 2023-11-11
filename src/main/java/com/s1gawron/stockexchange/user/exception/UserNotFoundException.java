package com.s1gawron.stockexchange.user.exception;

public class UserNotFoundException extends RuntimeException {

    private UserNotFoundException(final String message) {
        super(message);
    }

    public static UserNotFoundException create(final String username) {
        return new UserNotFoundException("User: " + username + " could not be found!");
    }
}
