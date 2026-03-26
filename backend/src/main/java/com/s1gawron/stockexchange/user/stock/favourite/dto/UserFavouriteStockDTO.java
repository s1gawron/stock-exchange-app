package com.s1gawron.stockexchange.user.stock.favourite.dto;

import com.s1gawron.stockexchange.user.stock.favourite.model.UserFavouriteStock;

public record UserFavouriteStockDTO(Long id, String ticker) {

    public static UserFavouriteStockDTO create(final UserFavouriteStock userFavouriteStock) {
        return new UserFavouriteStockDTO(userFavouriteStock.getId(), userFavouriteStock.getTicker());
    }
}
