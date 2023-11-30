package com.s1gawron.stockexchange.shared.helper;

import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserRole;

public enum UserCreatorHelper {

    I;

    private static final String ENCODED_PASSWORD = "$2a$10$PCo2iC74Ge/tI05PfPS9DOPQUjwdDtrheGwGEkuyB8myslYC2kKuK";

    public User createUser() {
        return new User(true, "testUser", "user@test.pl", ENCODED_PASSWORD, UserRole.USER);
    }

    public User createUser(final long userId, final String username) {
        return new User(userId, true, username, "user@test.pl", ENCODED_PASSWORD, UserRole.USER);
    }

    public User createDifferentUser() {
        return new User(true, "testUser2", "user2@test.pl", ENCODED_PASSWORD, UserRole.USER);
    }
}
