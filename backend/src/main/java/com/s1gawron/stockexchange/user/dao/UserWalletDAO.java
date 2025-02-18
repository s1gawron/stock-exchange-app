package com.s1gawron.stockexchange.user.dao;

import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;

import java.util.List;
import java.util.Optional;

public interface UserWalletDAO {

    Optional<UserWallet> findById(long walletId);

    Optional<UserWallet> findUserWalletByUserId(long userId);

    void saveUserWallet(UserWallet userWallet);

    List<UserStock> getUserStocks(long walletId);

    void updateUserWallet(UserWallet userWallet);

    Optional<UserStock> getUserStock(long walletId, String ticker);

    void updateUserStock(UserStock userStock);

    void saveUserStock(UserStock userStock);

    void deleteUserStock(UserStock userStock);

    List<Long> getAllWalletIds();

}
