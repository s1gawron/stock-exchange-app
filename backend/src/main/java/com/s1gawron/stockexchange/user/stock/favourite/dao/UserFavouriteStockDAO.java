package com.s1gawron.stockexchange.user.stock.favourite.dao;

import com.s1gawron.stockexchange.user.stock.favourite.model.UserFavouriteStock;

import java.util.List;
import java.util.Optional;

public interface UserFavouriteStockDAO {

    List<UserFavouriteStock> findByUserId(long userId);

    Optional<UserFavouriteStock> findByUserIdAndTicker(long userId, String ticker);

    void save(UserFavouriteStock userFavouriteStock);

    void delete(UserFavouriteStock userFavouriteStock);
}
