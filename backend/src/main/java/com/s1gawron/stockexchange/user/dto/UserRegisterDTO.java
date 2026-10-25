package com.s1gawron.stockexchange.user.dto;

import java.util.Locale;

public record UserRegisterDTO(String username, String email, String password) {

    public UserRegisterDTO {
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

}
