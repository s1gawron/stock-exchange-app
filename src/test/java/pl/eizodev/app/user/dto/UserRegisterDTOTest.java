package pl.eizodev.app.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import pl.eizodev.app.user.exception.UserEmailPatternViolationException;
import pl.eizodev.app.user.exception.UserPasswordTooWeakException;
import pl.eizodev.app.user.exception.UserRegisterEmptyPropertiesException;
import pl.eizodev.app.user.exception.UserWalletBalanceLessThanZeroException;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisterDTOTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        final String userRegisterJson = Files.readString(Path.of("src/test/resources/user-register.json"));
        final UserRegisterDTO result = mapper.readValue(userRegisterJson, UserRegisterDTO.class);

        assertEquals("test", result.getUsername());
        assertEquals("test@test.pl", result.getEmail());
        assertEquals("Start00!", result.getPassword());
        assertEquals(new BigDecimal("10000.00"), result.getUserWalletBalance());
    }

    @Test
    void shouldValidate() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "Start00!", BigDecimal.valueOf(10000));

        assertTrue(userRegisterDTO.validate());
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(null, "test@test.pl", "Start00!", BigDecimal.valueOf(10000));

        assertThrows(UserRegisterEmptyPropertiesException.class, userRegisterDTO::validate, "Username cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", null, "Start00!", BigDecimal.valueOf(10000));

        assertThrows(UserRegisterEmptyPropertiesException.class, userRegisterDTO::validate, "User email cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", null, BigDecimal.valueOf(10000));

        assertThrows(UserRegisterEmptyPropertiesException.class, userRegisterDTO::validate, "User password cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenUserWalletBalanceIsNull() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "Start00!", null);

        assertThrows(UserRegisterEmptyPropertiesException.class, userRegisterDTO::validate, "User wallet balance cannot be empty!");
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotMatchPattern() {
        final String email = "test-test.pl";
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", email, "Start00!", BigDecimal.valueOf(10000));

        assertThrows(UserEmailPatternViolationException.class, userRegisterDTO::validate,
            "Email: " + email + ", does not match validation pattern. If this is proper email please contact me for a fix.");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooWeak() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "password", BigDecimal.valueOf(10000));

        assertThrows(UserPasswordTooWeakException.class, userRegisterDTO::validate,
            "Provided password is too weak! The password must be minimum 8 characters long and contain upper and lower case letters, a number and one of the characters !, @, #, $, *");
    }

    @Test
    void shouldThrowExceptionWhenUserWalletBalanceIsLessThanZero() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test", "test@test.pl", "Start00!", BigDecimal.valueOf(-100));

        assertThrows(UserWalletBalanceLessThanZeroException.class, userRegisterDTO::validate, "User wallet balance cannot be less than zero!");
    }

}