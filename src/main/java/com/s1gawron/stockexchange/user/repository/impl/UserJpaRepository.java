package com.s1gawron.stockexchange.user.repository.impl;

import com.s1gawron.stockexchange.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface UserJpaRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT userId FROM User")
    List<Long> getAllUserIds();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

}