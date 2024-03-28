package com.s1gawron.stockexchange.transaction.exception;

import java.math.BigDecimal;

public class NotEnoughMoneyException extends RuntimeException {

    private NotEnoughMoneyException(final String message) {
        super(message);
    }

    public static NotEnoughMoneyException create(final BigDecimal transactionCost, final BigDecimal accountBalance, final BigDecimal maxAmountOfStockToPurchase,
        final String stockName) {
        return new NotEnoughMoneyException("You cannot perform this transaction! Transaction price is: " + transactionCost
            + " USD, but your account balance is: " + accountBalance + " USD. You can buy: " + maxAmountOfStockToPurchase + " of " + stockName + " stock");
    }
}
