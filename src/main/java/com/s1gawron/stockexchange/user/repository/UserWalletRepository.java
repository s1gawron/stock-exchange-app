package com.s1gawron.stockexchange.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.s1gawron.stockexchange.user.model.UserWallet;

import java.util.Optional;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {

    Optional<UserWallet> findByUser_Username(String username);

}
