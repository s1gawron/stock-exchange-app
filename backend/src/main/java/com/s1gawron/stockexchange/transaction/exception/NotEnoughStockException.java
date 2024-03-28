package com.s1gawron.stockexchange.transaction.exception;

public class NotEnoughStockException extends RuntimeException {

    private NotEnoughStockException(final String message) {
        super(message);
    }

    public static NotEnoughStockException create(final String stockName, int userStockQuantity, int transactionStockQuantity) {
        return new NotEnoughStockException(
            "You cannot perform this transaction! Transaction quantity is: " + transactionStockQuantity + ", but you only have: " + userStockQuantity + " of "
                + stockName + " stock");
    }
}
