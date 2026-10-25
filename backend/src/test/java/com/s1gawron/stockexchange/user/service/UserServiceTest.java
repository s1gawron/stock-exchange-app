package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import com.s1gawron.stockexchange.user.dao.impl.InMemoryUserDAO;
import com.s1gawron.stockexchange.user.dao.impl.InMemoryUserWalletDAO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserRole;
import com.s1gawron.stockexchange.user.model.UserWallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private static final String USERNAME = "testUser";

    private static final String EMAIL = "test@test.pl";

    private InMemoryUserDAO userDAO;

    private InMemoryUserWalletDAO userWalletDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAO = new InMemoryUserDAO();
        userWalletDAO = new InMemoryUserWalletDAO();

        userService = new UserService(userDAO, userWalletDAO, Mockito.mock(PasswordEncoder.class), CLOCK);
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExistsWhileRegisteringUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!");
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        userDAO.saveUser(user);

        assertThrows(UserNameExistsException.class, () -> userService.validateAndRegisterUser(userRegisterDTO));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsWhileRegisteringUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!");
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        userDAO.saveUser(user);

        final UserRegisterDTO differentNameUserRegisterDTO = new UserRegisterDTO("testUser2", EMAIL, "Start00!");
        assertThrows(UserEmailExistsException.class, () -> userService.validateAndRegisterUser(differentNameUserRegisterDTO));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsWithDifferentCaseWhileRegisteringUser() {
        final User user = new User(true, USERNAME, "Test@Test.pl", "encryptedPassword", UserRole.USER);

        userDAO.saveUser(user);

        final UserRegisterDTO differentNameUserRegisterDTO = new UserRegisterDTO("testUser2", "test@TEST.pl", "Start00!");
        assertThrows(UserEmailExistsException.class, () -> userService.validateAndRegisterUser(differentNameUserRegisterDTO));
    }

    @Test
    void shouldRegisterUserWithNormalizedEmailAndWalletCreatedAtClockTime() {
        userService.validateAndRegisterUser(new UserRegisterDTO(USERNAME, " Test@Test.PL ", "Start00!"));

        final User user = userDAO.findByFilter(UserFilterParam.createForUsername(USERNAME)).orElseThrow();
        final UserWallet userWallet = userWalletDAO.findUserWalletByUserId(user.getId()).orElseThrow();

        assertEquals(EMAIL, user.getEmail());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastDayUpdateDate());
    }

}
