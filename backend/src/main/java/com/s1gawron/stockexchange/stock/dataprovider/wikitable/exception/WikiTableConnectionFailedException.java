package com.s1gawron.stockexchange.stock.dataprovider.wikitable.exception;

public class WikiTableConnectionFailedException extends RuntimeException {

    private WikiTableConnectionFailedException(final String message) {
        super(message);
    }

    public static WikiTableConnectionFailedException create() {
        return new WikiTableConnectionFailedException("Received an empty response from wikitable! Please try again.");
    }

    public static WikiTableConnectionFailedException create(final int statusCode) {
        return new WikiTableConnectionFailedException("Connection to wikitable with url could not be obtained! Status code: " + statusCode);
    }

    public static WikiTableConnectionFailedException create(final String reason) {
        return new WikiTableConnectionFailedException("Connection to wikitable with url could not be obtained! Reason: " + reason);
    }

}
