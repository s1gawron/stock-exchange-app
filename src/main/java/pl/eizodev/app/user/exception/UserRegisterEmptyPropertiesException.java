package pl.eizodev.app.user.exception;

public class UserRegisterEmptyPropertiesException extends RuntimeException {

    private UserRegisterEmptyPropertiesException(final String message) {
        super(message);
    }

    public static UserRegisterEmptyPropertiesException createForName() {
        return new UserRegisterEmptyPropertiesException("Username cannot be empty!");
    }

    public static UserRegisterEmptyPropertiesException createForEmail() {
        return new UserRegisterEmptyPropertiesException("User email cannot be empty!");
    }

    public static UserRegisterEmptyPropertiesException createForPassword() {
        return new UserRegisterEmptyPropertiesException("User password cannot be empty!");
    }

    public static UserRegisterEmptyPropertiesException createForUserWalletBalance() {
        return new UserRegisterEmptyPropertiesException("User wallet balance cannot be empty!");
    }
}
