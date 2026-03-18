package com.s1gawron.stockexchange.transaction.exception;

import java.math.BigDecimal;

public class TransactionProcessingException extends RuntimeException {

    private TransactionProcessingException(final String message) {
        super(message);
    }

    public static TransactionProcessingException create() {
        return new TransactionProcessingException("The transaction did not meet the conditions for processing.");
    }

    public static TransactionProcessingException createForPurchase(final BigDecimal stockPriceLimit, final BigDecimal currentPrice) {
        return new TransactionProcessingException(
            "Could not perform transaction because current stock price: " + currentPrice + " is bigger than transaction purchase price: " + stockPriceLimit);
    }

    public static TransactionProcessingException createForSell(final BigDecimal stockPriceLimit, final BigDecimal currentPrice) {
        return new TransactionProcessingException(
            "Could not perform transaction because current stock price: " + currentPrice + " is lower than transaction sell price: " + stockPriceLimit);
    }
}
