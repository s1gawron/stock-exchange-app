package com.s1gawron.stockexchange.user.repository;

import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;

import java.util.List;
import java.util.Optional;

public interface UserWalletDAO {

    Optional<UserWallet> findUserWalletByUserId(long userId);

    void saveUserWallet(UserWallet userWallet);

    List<UserStock> getUserStocks(Long walletId);

    void updateUserWallet(UserWallet userWallet);

}
