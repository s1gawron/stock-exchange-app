package com.s1gawron.stockexchange.user.dao.filter;

import java.util.Optional;

public record UserFilterParam(String username, String email) {

    public static UserFilterParam createForUsername(final String username) {
        return new UserFilterParam(username, null);
    }

    public static UserFilterParam createForEmail(final String email) {
        return new UserFilterParam(null, email);
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

}
