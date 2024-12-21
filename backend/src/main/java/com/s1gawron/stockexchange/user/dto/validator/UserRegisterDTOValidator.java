package com.s1gawron.stockexchange.user.dto.validator;

import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailPatternViolationException;
import com.s1gawron.stockexchange.user.exception.UserPasswordTooWeakException;
import com.s1gawron.stockexchange.user.exception.UserRegisterEmptyPropertiesException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum UserRegisterDTOValidator {

    I;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$*])(?!.*\\s).{8,32}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public boolean validate(final UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO.username() == null) {
            throw UserRegisterEmptyPropertiesException.createForName();
        }

        if (userRegisterDTO.email() == null) {
            throw UserRegisterEmptyPropertiesException.createForEmail();
        }

        if (userRegisterDTO.password() == null) {
            throw UserRegisterEmptyPropertiesException.createForPassword();
        }

        final Matcher emailMatcher = EMAIL_PATTERN.matcher(userRegisterDTO.email());

        if (!emailMatcher.matches()) {
            throw UserEmailPatternViolationException.create(userRegisterDTO.email());
        }

        final Matcher passwordMatcher = PASSWORD_PATTERN.matcher(userRegisterDTO.password());

        if (!passwordMatcher.matches()) {
            throw UserPasswordTooWeakException.create();
        }

        return true;
    }

}
