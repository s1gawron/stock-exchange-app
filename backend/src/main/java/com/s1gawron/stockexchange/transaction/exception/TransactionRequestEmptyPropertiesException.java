package com.s1gawron.stockexchange.transaction.exception;

public class TransactionRequestEmptyPropertiesException extends RuntimeException {

    private TransactionRequestEmptyPropertiesException(final String message) {
        super(message);
    }

    public static TransactionRequestEmptyPropertiesException createForTransactionType() {
        return new TransactionRequestEmptyPropertiesException("Transaction type cannot be empty!");
    }

    public static TransactionRequestEmptyPropertiesException createForStockTicker() {
        return new TransactionRequestEmptyPropertiesException("Stock ticker cannot be empty!");
    }

    public static TransactionRequestEmptyPropertiesException createForPrice() {
        return new TransactionRequestEmptyPropertiesException("Transaction price cannot be empty!");
    }

}
