package pl.eizodev.app.service.exception;

public class UserNameExistsException extends RuntimeException {

    private UserNameExistsException(final String message) {
        super(message);
    }

    public static UserNameExistsException create() {
        return new UserNameExistsException("Account with given username already exists!");
    }
}
