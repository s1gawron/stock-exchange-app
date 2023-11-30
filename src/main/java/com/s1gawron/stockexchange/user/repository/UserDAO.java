package com.s1gawron.stockexchange.user.repository;

import com.s1gawron.stockexchange.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    List<Long> getAllUserIds();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    void saveUser(User user);

    void deleteUser(User user);

}
