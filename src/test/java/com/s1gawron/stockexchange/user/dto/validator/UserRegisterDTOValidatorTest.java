package com.s1gawron.stockexchange.user.dto.validator;

import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailPatternViolationException;
import com.s1gawron.stockexchange.user.exception.UserPasswordTooWeakException;
import com.s1gawron.stockexchange.user.exception.UserRegisterEmptyPropertiesException;
import com.s1gawron.stockexchange.user.exception.UserWalletBalanceLessThanZeroException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisterDTOValidatorTest {

    @Test
    void shouldValidate() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "Start00!", BigDecimal.valueOf(10000));

        assertTrue(UserRegisterDTOValidator.I.validate(userRegisterDTO));
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(null, "test@test.pl", "Start00!", BigDecimal.valueOf(10000));

        Assertions.assertThrows(UserRegisterEmptyPropertiesException.class, () -> UserRegisterDTOValidator.I.validate(userRegisterDTO),
            "Username cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", null, "Start00!", BigDecimal.valueOf(10000));

        assertThrows(UserRegisterEmptyPropertiesException.class, () -> UserRegisterDTOValidator.I.validate(userRegisterDTO), "User email cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", null, BigDecimal.valueOf(10000));

        assertThrows(UserRegisterEmptyPropertiesException.class, () -> UserRegisterDTOValidator.I.validate(userRegisterDTO), "User password cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenUserWalletBalanceIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "Start00!", null);

        assertThrows(UserRegisterEmptyPropertiesException.class, () -> UserRegisterDTOValidator.I.validate(userRegisterDTO),
            "User wallet balance cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotMatchPattern() {
        final String email = "test-test.pl";
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", email, "Start00!", BigDecimal.valueOf(10000));

        Assertions.assertThrows(UserEmailPatternViolationException.class, () -> UserRegisterDTOValidator.I.validate(userRegisterDTO),
            "Email: " + email + ", does not match validation pattern. If this is proper email please contact me for a fix.");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooWeak() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "password", BigDecimal.valueOf(10000));

        Assertions.assertThrows(UserPasswordTooWeakException.class, () -> UserRegisterDTOValidator.I.validate(userRegisterDTO),
            "Provided password is too weak! The password must be minimum 8 characters long and contain upper and lower case letters, a number and one of the characters !, @, #, $, *");
    }

    @Test
    void shouldThrowExceptionWhenUserWalletBalanceIsLessThanZero() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "Start00!", BigDecimal.valueOf(-100));

        Assertions.assertThrows(UserWalletBalanceLessThanZeroException.class, () -> UserRegisterDTOValidator.I.validate(userRegisterDTO),
            "User wallet balance cannot be less than zero!");
    }

}