package pl.eizodev.app.services.exceptions;

import java.math.BigDecimal;

public class NotEnoughMoneyException extends RuntimeException {

    private NotEnoughMoneyException(final String message) {
        super(message);
    }

    public static NotEnoughMoneyException create(final BigDecimal transactionCost, final BigDecimal accountBalance,
                                                 final String maxAmountOfStockToPurchase, final String stockName) {
        return new NotEnoughMoneyException("You cannot perform this transaction! Transaction price is: "
                + transactionCost + ", but your account balance is: " + accountBalance + " You can buy: " + maxAmountOfStockToPurchase + " of " +
                stockName + " stock");
    }
}
