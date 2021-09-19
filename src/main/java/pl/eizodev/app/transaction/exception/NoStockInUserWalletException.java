package pl.eizodev.app.transaction.exception;

public class NoStockInUserWalletException extends RuntimeException {

    private NoStockInUserWalletException(final String message) {
        super(message);
    }

    public static NoStockInUserWalletException create(final String stockName) {
        return new NoStockInUserWalletException("You dont have any stock of " + stockName + " in your wallet");
    }
}
