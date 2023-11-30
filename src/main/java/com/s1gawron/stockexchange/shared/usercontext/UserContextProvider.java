package com.s1gawron.stockexchange.shared.usercontext;

import com.s1gawron.stockexchange.shared.exception.UserUnauthenticatedException;
import com.s1gawron.stockexchange.user.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public enum UserContextProvider {

    I;

    public User getLoggedInUser() {
        final Optional<Object> principal = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return principal.map(p -> (User) p).orElseThrow(UserUnauthenticatedException::create);
    }
}
