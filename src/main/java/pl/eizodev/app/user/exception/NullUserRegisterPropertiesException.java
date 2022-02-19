package pl.eizodev.app.user.exception;

public class NullUserRegisterPropertiesException extends RuntimeException {

    private NullUserRegisterPropertiesException(final String message) {
        super(message);
    }

    public static NullUserRegisterPropertiesException createForName() {
        return new NullUserRegisterPropertiesException("Username cannot be empty!");
    }

    public static NullUserRegisterPropertiesException createForEmail() {
        return new NullUserRegisterPropertiesException("User email cannot be empty!");
    }

    public static NullUserRegisterPropertiesException createForPassword() {
        return new NullUserRegisterPropertiesException("User password cannot be empty!");
    }

    public static NullUserRegisterPropertiesException createForUserBalance() {
        return new NullUserRegisterPropertiesException("User balance cannot be empty!");
    }
}
