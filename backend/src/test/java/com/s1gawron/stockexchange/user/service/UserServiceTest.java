package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dao.impl.InMemoryUserDAO;
import com.s1gawron.stockexchange.user.dao.impl.InMemoryUserWalletDAO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private static final String USERNAME = "testUser";

    private static final String EMAIL = "test@test.pl";

    private InMemoryUserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAO = new InMemoryUserDAO();

        userService = new UserService(userDAO, new InMemoryUserWalletDAO());
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

}