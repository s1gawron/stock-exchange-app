package pl.eizodev.app.user.exception;

public class UserEmailPatternViolationException extends RuntimeException {

    private UserEmailPatternViolationException(final String message) {
        super(message);
    }

    public static UserEmailPatternViolationException create(final String email) {
        return new UserEmailPatternViolationException(
            "Email: " + email + ", does not match validation pattern. If this is proper email please contact me for a fix.");
    }

}
