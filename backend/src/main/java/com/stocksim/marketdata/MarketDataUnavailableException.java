package com.stocksim.marketdata;

/** The vendor could not be reached, refused the call, or the local rate limit was hit. */
public class MarketDataUnavailableException extends RuntimeException {

	public MarketDataUnavailableException(String message) {
		super(message);
	}

	public MarketDataUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
