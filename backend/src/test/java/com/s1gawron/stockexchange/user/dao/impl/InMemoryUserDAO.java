package com.s1gawron.stockexchange.user.dao.impl;

import com.s1gawron.stockexchange.user.dao.UserDAO;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import com.s1gawron.stockexchange.user.model.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryUserDAO implements UserDAO {

    private final List<User> users = new ArrayList<>();

    @Override public Optional<User> findByFilter(final UserFilterParam filterParam) {
        final Optional<String> username = filterParam.getUsername();

        if (username.isPresent()) {
            return users.stream()
                .filter(user -> username.get().equals(user.getUsername()))
                .findFirst();
        }

        final Optional<String> email = filterParam.getEmail();

        if (email.isPresent()) {
            return users.stream()
                .filter(user -> email.get().equals(user.getEmail()))
                .findFirst();
        }

        return Optional.empty();
    }

    @Override public void saveUser(final User user) {
        ReflectionTestUtils.setField(user, "id", (long) users.size());
        users.add(user);
    }
}
