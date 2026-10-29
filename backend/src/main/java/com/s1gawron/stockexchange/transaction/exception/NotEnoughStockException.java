package com.s1gawron.stockexchange.transaction.exception;

import java.math.BigDecimal;

public class NotEnoughStockException extends RuntimeException {

    private NotEnoughStockException(final String message) {
        super(message);
    }

    public static NotEnoughStockException create(final String stockName, final BigDecimal userStockQuantity, final BigDecimal transactionStockQuantity) {
        return new NotEnoughStockException(
            "You cannot perform this transaction! Transaction quantity is: " + transactionStockQuantity.stripTrailingZeros().toPlainString()
                + ", but you only have: " + userStockQuantity.stripTrailingZeros().toPlainString() + " of " + stockName + " stock");
    }

    public static NotEnoughStockException createForPosition(final String stockName, final long positionId, final BigDecimal positionStockQuantity,
        final BigDecimal transactionStockQuantity) {
        return new NotEnoughStockException(
            "You cannot perform this transaction! Transaction quantity is: " + transactionStockQuantity.stripTrailingZeros().toPlainString()
                + ", but you only have: " + positionStockQuantity.stripTrailingZeros().toPlainString() + " of " + stockName + " stock in position #"
                + positionId);
    }
}
