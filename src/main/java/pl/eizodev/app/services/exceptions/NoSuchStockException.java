package pl.eizodev.app.services.exceptions;

public class NoSuchStockException extends RuntimeException {

    private NoSuchStockException(final String message) {
        super(message);
    }

    public static NoSuchStockException create(final String stockName) {
        return new NoSuchStockException("You dont have any stock of: " + stockName + " in your wallet");
    }
}
