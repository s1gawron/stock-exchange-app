package com.s1gawron.stockexchange.user.dao.impl;

import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import com.s1gawron.stockexchange.user.model.UserStockPosition;
import com.s1gawron.stockexchange.user.model.UserWallet;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InMemoryUserWalletDAO implements UserWalletDAO {

    private final List<UserWallet> userWallets = new ArrayList<>();

    private final List<UserStockPosition> userStockPositions = new ArrayList<>();

    private long nextUserStockPositionId = 1;

    @Override public Optional<UserWallet> findById(final long walletId) {
        return userWallets.stream()
            .filter(wallet -> wallet.getId() == walletId)
            .findFirst();
    }

    @Override public Optional<UserWallet> findByIdForUpdate(final long walletId) {
        return findById(walletId);
    }

    @Override public Optional<UserWallet> findUserWalletByUserId(final long userId) {
        return userWallets.stream()
            .filter(wallet -> wallet.getUserId() == userId)
            .findFirst();
    }

    @Override public Optional<UserWallet> findUserWalletByUserIdForUpdate(final long userId) {
        return findUserWalletByUserId(userId);
    }

    @Override public void saveUserWallet(final UserWallet userWallet) {
        ReflectionTestUtils.setField(userWallet, "id", (long) userWallets.size());
        userWallets.add(userWallet);
    }

    @Override public List<UserStockPosition> getUserStockPositions(final long walletId) {
        return userStockPositions.stream()
            .filter(position -> position.getWalletId() == walletId)
            .sorted(Comparator.comparing(UserStockPosition::getOpenDate).thenComparing(UserStockPosition::getId))
            .toList();
    }

    @Override public void updateUserWallet(final UserWallet updatedUserWallet) {
        for (int i = 0; i < userWallets.size(); i++) {
            if (userWallets.get(i).getId().equals(updatedUserWallet.getId())) {
                userWallets.set(i, updatedUserWallet);
                break;
            }
        }
    }

    @Override public List<UserStockPosition> getUserStockPositions(final long walletId, final String ticker) {
        return getUserStockPositions(walletId).stream()
            .filter(position -> position.getTicker().equals(ticker))
            .toList();
    }

    @Override public void updateUserStockPosition(final UserStockPosition updatedUserStockPosition) {
        for (int i = 0; i < userStockPositions.size(); i++) {
            if (userStockPositions.get(i).getId().equals(updatedUserStockPosition.getId())) {
                userStockPositions.set(i, updatedUserStockPosition);
                break;
            }
        }
    }

    @Override public void saveUserStockPosition(final UserStockPosition userStockPosition) {
        ReflectionTestUtils.setField(userStockPosition, "id", nextUserStockPositionId++);
        userStockPositions.add(userStockPosition);
    }

    @Override public void deleteUserStockPosition(final UserStockPosition userStockPosition) {
        userStockPositions.removeIf(position -> position.getId().equals(userStockPosition.getId()));
    }

    @Override public List<Long> getAllWalletIds() {
        return userWallets.stream()
            .map(UserWallet::getId)
            .toList();
    }
}
