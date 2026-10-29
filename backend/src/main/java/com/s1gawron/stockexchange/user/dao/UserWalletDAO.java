package com.s1gawron.stockexchange.user.dao;

import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.model.UserWallet;

import java.util.List;
import java.util.Optional;

public interface UserWalletDAO {

    Optional<UserWallet> findById(long walletId);

    Optional<UserWallet> findByIdForUpdate(long walletId);

    Optional<UserWallet> findUserWalletByUserId(long userId);

    Optional<UserWallet> findUserWalletByUserIdForUpdate(long userId);

    void saveUserWallet(UserWallet userWallet);

    List<UserStockPosition> getUserStockPositions(long walletId);

    void updateUserWallet(UserWallet userWallet);

    List<UserStockPosition> getUserStockPositions(long walletId, String ticker);

    void updateUserStockPosition(UserStockPosition userStockPosition);

    void saveUserStockPosition(UserStockPosition userStockPosition);

    void deleteUserStockPosition(UserStockPosition userStockPosition);

    List<Long> getAllWalletIds();

}
