package com.s1gawron.stockexchange.user.stock.favourite.dao.impl;

import com.s1gawron.stockexchange.user.stock.favourite.dao.UserFavouriteStockDAO;
import com.s1gawron.stockexchange.user.stock.favourite.model.UserFavouriteStock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryUserFavouriteStockDAO implements UserFavouriteStockDAO {

    private final List<UserFavouriteStock> favouriteStocks = new ArrayList<>();

    @Override
    public List<UserFavouriteStock> findByUserId(final long userId) {
        return favouriteStocks.stream()
            .filter(stock -> stock.getUserId() == userId)
            .toList();
    }

    @Override
    public Optional<UserFavouriteStock> findByUserIdAndTicker(final long userId, final String ticker) {
        return favouriteStocks.stream()
            .filter(stock -> stock.getUserId() == userId && stock.getTicker().equals(ticker))
            .findFirst();
    }

    @Override
    public void save(final UserFavouriteStock userFavouriteStock) {
        ReflectionTestUtils.setField(userFavouriteStock, "id", (long) favouriteStocks.size());
        favouriteStocks.add(userFavouriteStock);
    }

    @Override
    public void delete(final UserFavouriteStock userFavouriteStock) {
        for (int i = 0; i < favouriteStocks.size(); i++) {
            if (favouriteStocks.get(i).getId().equals(userFavouriteStock.getId())) {
                favouriteStocks.remove(i);
                break;
            }
        }
    }
}
