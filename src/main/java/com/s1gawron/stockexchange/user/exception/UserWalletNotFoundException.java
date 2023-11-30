package com.s1gawron.stockexchange.user.exception;

public class UserWalletNotFoundException extends RuntimeException {

    private UserWalletNotFoundException(final String message) {
        super(message);
    }

    public static UserWalletNotFoundException create(final long userId) {
        return new UserWalletNotFoundException("Wallet for userId: " + userId + " could not be found!");
    }

}
