package com.s1gawron.stockexchange.user.stock.favourite.service;

import com.s1gawron.stockexchange.shared.usercontext.UserContextProvider;
import com.s1gawron.stockexchange.user.stock.favourite.dao.UserFavouriteStockDAO;
import com.s1gawron.stockexchange.user.stock.favourite.dto.AddFavouriteStockRequestDTO;
import com.s1gawron.stockexchange.user.stock.favourite.dto.UserFavouriteStockDTO;
import com.s1gawron.stockexchange.user.stock.favourite.exception.UserFavouriteStockAlreadyExistsException;
import com.s1gawron.stockexchange.user.stock.favourite.exception.UserFavouriteStockNotFoundException;
import com.s1gawron.stockexchange.user.stock.favourite.model.UserFavouriteStock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserFavouriteStockService {

    private final UserFavouriteStockDAO userFavouriteStockDAO;

    public UserFavouriteStockService(final UserFavouriteStockDAO userFavouriteStockDAO) {
        this.userFavouriteStockDAO = userFavouriteStockDAO;
    }

    @Transactional
    public UserFavouriteStockDTO addFavouriteStock(final AddFavouriteStockRequestDTO requestDTO) {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();

        userFavouriteStockDAO.findByUserIdAndTicker(userId, requestDTO.ticker()).ifPresent(existing -> {
            throw UserFavouriteStockAlreadyExistsException.create(existing.getTicker());
        });

        final UserFavouriteStock favourite = UserFavouriteStock.create(userId, requestDTO.ticker());
        userFavouriteStockDAO.save(favourite);

        return UserFavouriteStockDTO.create(favourite);
    }

    @Transactional
    public void removeFavouriteStock(final String ticker) {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();
        final UserFavouriteStock favourite = userFavouriteStockDAO.findByUserIdAndTicker(userId, ticker)
            .orElseThrow(() -> UserFavouriteStockNotFoundException.create(ticker));

        userFavouriteStockDAO.delete(favourite);
    }

    @Transactional(readOnly = true)
    public List<UserFavouriteStockDTO> getUserFavouriteStocks() {
        final long userId = UserContextProvider.I.getLoggedInUser().getId();
        return userFavouriteStockDAO.findByUserId(userId).stream()
            .map(UserFavouriteStockDTO::create)
            .toList();
    }
}
