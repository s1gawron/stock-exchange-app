package com.s1gawron.stockexchange.user.dto.validator;

import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailPatternViolationException;
import com.s1gawron.stockexchange.user.exception.UserPasswordTooWeakException;
import com.s1gawron.stockexchange.user.exception.UserRegisterEmptyPropertiesException;
import com.s1gawron.stockexchange.user.exception.UserWalletBalanceLessThanZeroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum UserDTOValidator {

    I;

    private static final Logger log = LoggerFactory.getLogger(UserDTOValidator.class);

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$*])(?!.*\\s).{8,32}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public boolean validate(final UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO.username() == null) {
            log.error("Username was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForName();
        }

        if (userRegisterDTO.email() == null) {
            log.error("Email was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForEmail();
        }

        if (userRegisterDTO.password() == null) {
            log.error("Password was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForPassword();
        }

        if (userRegisterDTO.userWalletBalance() == null) {
            log.error("Wallet balance was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForUserWalletBalance();
        }

        final Matcher emailMatcher = EMAIL_PATTERN.matcher(userRegisterDTO.email());

        if (!emailMatcher.matches()) {
            log.error("Provided email: {}, does not match pattern in registration process", userRegisterDTO.email());
            throw UserEmailPatternViolationException.create(userRegisterDTO.email());
        }

        final Matcher passwordMatcher = PASSWORD_PATTERN.matcher(userRegisterDTO.password());

        if (!passwordMatcher.matches()) {
            log.error("Password does not meet security policy in registration process");
            throw UserPasswordTooWeakException.create();
        }

        if (userRegisterDTO.userWalletBalance().compareTo(BigDecimal.ZERO) < 0) {
            log.error("User wallet balance cannot be less than zero!");
            throw UserWalletBalanceLessThanZeroException.create();
        }

        return true;
    }

}
