package com.s1gawron.stockexchange.transaction.exception;

public class WalletCurrencyMismatchException extends RuntimeException {

    private WalletCurrencyMismatchException(final String message) {
        super(message);
    }

    public static WalletCurrencyMismatchException create(final String stockName, final String stockCurrency, final String walletCurrency) {
        return new WalletCurrencyMismatchException("You cannot perform this transaction! " + stockName + " stock is traded in " + stockCurrency
            + ", but your wallet is in " + walletCurrency);
    }
}
