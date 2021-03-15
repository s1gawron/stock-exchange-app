package pl.eizodev.app.services.exceptions;

public class NotEnoughStockException extends RuntimeException {

    private NotEnoughStockException(final String message) {
        super(message);
    }

    public static NotEnoughStockException create(final String stockName, int stockQuantity, int transactionStockQuantity) {
        return new NotEnoughStockException("You want to sell: " + transactionStockQuantity + " stocks of " + stockName +
                ", but you only have: " + stockQuantity);
    }
}
