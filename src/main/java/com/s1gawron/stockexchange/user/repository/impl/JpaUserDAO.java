package com.s1gawron.stockexchange.user.repository.impl;

import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.repository.UserDAO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUserDAO implements UserDAO {

    private final UserJpaRepository userJpaRepository;

    JpaUserDAO(final UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override public List<Long> getAllUserIds() {
        return userJpaRepository.getAllUserIds();
    }

    @Override public Optional<User> findByUsername(final String username) {
        return userJpaRepository.findByUsername(username);
    }

    @Override public Optional<User> findByEmail(final String email) {
        return userJpaRepository.findByEmail(email);
    }

    @Override public void saveUser(final User user) {
        userJpaRepository.save(user);
    }

    @Override public void deleteUser(final User user) {
        userJpaRepository.delete(user);
    }
}
