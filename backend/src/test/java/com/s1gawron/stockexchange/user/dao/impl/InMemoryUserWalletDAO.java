package com.s1gawron.stockexchange.user.dao.impl;

import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryUserWalletDAO implements UserWalletDAO {

    private final List<UserWallet> userWallets = new ArrayList<>();

    private final List<UserStock> userStocks = new ArrayList<>();

    @Override public Optional<UserWallet> findById(final long walletId) {
        return userWallets.stream()
            .filter(wallet -> wallet.getId() == walletId)
            .findFirst();
    }

    @Override public Optional<UserWallet> findUserWalletByUserId(final long userId) {
        return userWallets.stream()
            .filter(wallet -> wallet.getUserId() == userId)
            .findFirst();
    }

    @Override public void saveUserWallet(final UserWallet userWallet) {
        ReflectionTestUtils.setField(userWallet, "id", (long) userWallets.size());
        userWallets.add(userWallet);
    }

    @Override public List<UserStock> getUserStocks(final long walletId) {
        return userStocks.stream()
            .filter(stock -> stock.getWalletId() == walletId)
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

    @Override public Optional<UserStock> getUserStock(final long walletId, final String ticker) {
        return userStocks.stream()
            .filter(stock -> stock.getWalletId() == walletId && stock.getTicker().equals(ticker))
            .findFirst();
    }

    @Override public void updateUserStock(final UserStock updatedUserStock) {
        for (int i = 0; i < userStocks.size(); i++) {
            if (userStocks.get(i).getId().equals(updatedUserStock.getId())) {
                userStocks.set(i, updatedUserStock);
                break;
            }
        }
    }

    @Override public void saveUserStock(final UserStock userStock) {
        ReflectionTestUtils.setField(userStock, "id", (long) userStocks.size());
        userStocks.add(userStock);
    }

    @Override public void deleteUserStock(final UserStock userStock) {
        for (int i = 0; i < userStocks.size(); i++) {
            if (userStocks.get(i).getId().equals(userStock.getId())) {
                userStocks.remove(i);
                break;
            }
        }
    }

    @Override public List<Long> getAllWalletIds() {
        return userWallets.stream()
            .map(UserWallet::getId)
            .toList();
    }
}
