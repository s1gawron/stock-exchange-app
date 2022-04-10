package pl.eizodev.app.user.exception;

public class UserWalletNotFoundException extends RuntimeException {

    private UserWalletNotFoundException(final String message) {
        super(message);
    }

    public static UserWalletNotFoundException create(final String username) {
        return new UserWalletNotFoundException("Wallet for user: " + username + " could not be found!");
    }

}
