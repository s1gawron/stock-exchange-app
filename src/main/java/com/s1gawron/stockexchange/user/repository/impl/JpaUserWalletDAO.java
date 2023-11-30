package com.s1gawron.stockexchange.user.repository.impl;

import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserWalletDAO;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserWalletDAO implements UserWalletDAO {

    private final UserWalletJpaRepository userWalletJpaRepository;

    JpaUserWalletDAO(final UserWalletJpaRepository userWalletJpaRepository) {
        this.userWalletJpaRepository = userWalletJpaRepository;
    }

    @Override public Optional<UserWallet> findUserWalletByUserId(final long userId) {
        return userWalletJpaRepository.findUserWalletByUserId(userId);
    }
}
