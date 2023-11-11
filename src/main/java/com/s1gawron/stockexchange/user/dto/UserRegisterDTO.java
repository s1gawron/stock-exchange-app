package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import com.s1gawron.stockexchange.user.exception.UserEmailPatternViolationException;
import com.s1gawron.stockexchange.user.exception.UserPasswordTooWeakException;
import com.s1gawron.stockexchange.user.exception.UserRegisterEmptyPropertiesException;
import com.s1gawron.stockexchange.user.exception.UserWalletBalanceLessThanZeroException;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Log4j2
@Builder
@Getter
@JsonDeserialize(builder = UserRegisterDTO.UserRegisterDTOBuilder.class)
public class UserRegisterDTO {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$*])(?!.*\\s).{8,32}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    private final String username;

    private final String email;

    private final String password;

    private final BigDecimal userWalletBalance;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserRegisterDTOBuilder {

    }

    public boolean validate() {
        if (username == null) {
            log.error("Username was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForName();
        }

        if (email == null) {
            log.error("Email was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForEmail();
        }

        if (password == null) {
            log.error("Password was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForPassword();
        }

        if (userWalletBalance == null) {
            log.error("Wallet balance was left empty in registration process");
            throw UserRegisterEmptyPropertiesException.createForUserWalletBalance();
        }

        final Matcher emailMatcher = EMAIL_PATTERN.matcher(email);

        if (!emailMatcher.matches()) {
            log.error("Provided email: {}, does not match pattern in registration process", email);
            throw UserEmailPatternViolationException.create(email);
        }

        final Matcher passwordMatcher = PASSWORD_PATTERN.matcher(password);

        if (!passwordMatcher.matches()) {
            log.error("Password does not meet security policy in registration process");
            throw UserPasswordTooWeakException.create();
        }

        if (userWalletBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.error("User wallet balance cannot be less than zero!");
            throw UserWalletBalanceLessThanZeroException.create();
        }

        return true;
    }
}
