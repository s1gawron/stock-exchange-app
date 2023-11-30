package com.s1gawron.stockexchange.user.repository.impl;

import com.s1gawron.stockexchange.user.model.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface UserWalletJpaRepository extends JpaRepository<UserWallet, Long> {

    @Query(value = "SELECT wallet FROM UserWallet wallet WHERE wallet.user.userId = :userId")
    Optional<UserWallet> findUserWalletByUserId(@Param(value = "userId") long userId);

}
