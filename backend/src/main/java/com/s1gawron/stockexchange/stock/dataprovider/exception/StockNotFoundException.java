package com.s1gawron.stockexchange.stock.dataprovider.exception;

public class StockNotFoundException extends RuntimeException {

    private StockNotFoundException(final String message) {
        super(message);
    }

    public static StockNotFoundException createFromTicker(final String ticker) {
        return new StockNotFoundException("Stock with ticker " + ticker + " could not be found!");
    }
}
