package com.s1gawron.stockexchange.stock.dataprovider.finnhub.exception;

public class FinnhubConnectionFailedException extends RuntimeException {

    private FinnhubConnectionFailedException(final String message) {
        super(message);
    }

    public static FinnhubConnectionFailedException create() {
        return new FinnhubConnectionFailedException("Received an empty response from finnhub! Please try again.");
    }

    public static FinnhubConnectionFailedException create(final int statusCode) {
        return new FinnhubConnectionFailedException("Connection to finnhub with url could not be obtained! Status code: " + statusCode);
    }

    public static FinnhubConnectionFailedException create(final String reason) {
        return new FinnhubConnectionFailedException("Connection to finnhub with url could not be obtained! Reason: " + reason);
    }

}
