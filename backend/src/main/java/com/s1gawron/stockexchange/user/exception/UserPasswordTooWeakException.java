package com.s1gawron.stockexchange.user.exception;

public class UserPasswordTooWeakException extends RuntimeException {

    private UserPasswordTooWeakException(final String message) {
        super(message);
    }

    public static UserPasswordTooWeakException create() {
        return new UserPasswordTooWeakException(
            "Provided password is too weak! The password must be minimum 8 characters long and contain upper and lower case letters, a number and one of the characters !, @, #, $, *");
    }

}
