package pl.eizodev.app.services.exceptions;

public class AccountNotFoundException extends RuntimeException {

    private AccountNotFoundException(final String message) {
        super(message);
    }

    public static AccountNotFoundException create(final String accountName) {
        return new AccountNotFoundException("Account " + accountName + " could not be found!");
    }
}
