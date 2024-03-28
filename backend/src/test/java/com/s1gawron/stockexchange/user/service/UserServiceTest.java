package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.dao.UserDAO;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private static final String USERNAME = "testUser";

    private static final UserFilterParam USERNAME_FILTER = UserFilterParam.createForUsername(USERNAME);

    private static final BigDecimal WALLET_BALANCE = new BigDecimal("10000.00");

    private static final String EMAIL = "test@test.pl";

    private static final UserFilterParam EMAIL_FILTER = UserFilterParam.createForEmail(EMAIL);

    private UserDAO userDAOMock;

    private UserWalletDAO userWalletDAOMock;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAOMock = Mockito.mock(UserDAO.class);
        userWalletDAOMock = Mockito.mock(UserWalletDAO.class);

        userService = new UserService(userDAOMock, userWalletDAOMock);
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExistsWhileRegisteringUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        Mockito.when(userDAOMock.findByFilter(USERNAME_FILTER)).thenReturn(Optional.of(user));

        assertThrows(UserNameExistsException.class, () -> userService.validateAndRegisterUser(userRegisterDTO));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsWhileRegisteringUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        Mockito.when(userDAOMock.findByFilter(EMAIL_FILTER)).thenReturn(Optional.of(user));

        assertThrows(UserEmailExistsException.class, () -> userService.validateAndRegisterUser(userRegisterDTO));
    }

}