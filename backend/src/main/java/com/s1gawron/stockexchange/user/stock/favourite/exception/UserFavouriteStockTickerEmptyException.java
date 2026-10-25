package com.s1gawron.stockexchange.user.stock.favourite.exception;

public class UserFavouriteStockTickerEmptyException extends RuntimeException {

    private UserFavouriteStockTickerEmptyException(final String message) {
        super(message);
    }

    public static UserFavouriteStockTickerEmptyException create() {
        return new UserFavouriteStockTickerEmptyException("Favourite stock ticker cannot be empty!");
    }
}
