package com.s1gawron.stockexchange.transaction.exception;

public class NoStockPositionInUserWalletException extends RuntimeException {

    private NoStockPositionInUserWalletException(final String message) {
        super(message);
    }

    public static NoStockPositionInUserWalletException create(final String stockName, final long positionId) {
        return new NoStockPositionInUserWalletException("You dont have position #" + positionId + " of " + stockName + " stock in your wallet");
    }
}
