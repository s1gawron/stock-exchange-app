package com.s1gawron.stockexchange.user.stock.favourite.exception;

public class UserFavouriteStockAlreadyExistsException extends RuntimeException {

    private UserFavouriteStockAlreadyExistsException(final String message) {
        super(message);
    }

    public static UserFavouriteStockAlreadyExistsException create(final String ticker) {
        return new UserFavouriteStockAlreadyExistsException("Stock '" + ticker + "' is already in favourites!");
    }
}
