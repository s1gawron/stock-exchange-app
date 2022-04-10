package pl.eizodev.app.user.exception;

public class UserWalletBalanceLessThanZeroException extends RuntimeException {

    private UserWalletBalanceLessThanZeroException(final String message) {
        super(message);
    }

    public static UserWalletBalanceLessThanZeroException create() {
        return new UserWalletBalanceLessThanZeroException("User wallet balance cannot be less than zero!");
    }

}
