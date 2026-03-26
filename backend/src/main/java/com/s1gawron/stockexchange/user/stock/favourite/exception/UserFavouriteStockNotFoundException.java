package com.s1gawron.stockexchange.user.stock.favourite.exception;

public class UserFavouriteStockNotFoundException extends RuntimeException {

    private UserFavouriteStockNotFoundException(final String message) {
        super(message);
    }

    public static UserFavouriteStockNotFoundException create(final String ticker) {
        return new UserFavouriteStockNotFoundException("Favourite stock '" + ticker + "' could not be found!");
    }
}
