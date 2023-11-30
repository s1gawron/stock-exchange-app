package com.s1gawron.stockexchange.user.repository;

import com.s1gawron.stockexchange.user.model.UserWallet;

import java.util.Optional;

public interface UserWalletDAO {

    Optional<UserWallet> findUserWalletByUserId(long userId);

}
