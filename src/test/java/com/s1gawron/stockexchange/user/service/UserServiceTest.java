package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserRole;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static final String USERNAME = "testUser";

    private static final BigDecimal WALLET_BALANCE = new BigDecimal("10000.00");

    private static final String EMAIL = "test@test.pl";

    private UserDAO userDAOMock;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAOMock = Mockito.mock(UserDAO.class);
        userService = new UserService(userDAOMock);
    }

    @Test
    void shouldFindUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "password", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");
        final UserWallet userWallet = UserWallet.createNewUserWallet(user, WALLET_BALANCE);
        user.setUserWallet(userWallet);

        Mockito.when(userDAOMock.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        final Optional<User> result = userService.getUser(USERNAME);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().getUsername());
        Assertions.assertEquals(UserRole.USER, result.get().getUserRole());
        assertEquals(0, result.get().getUserWallet().getUserStocks().size());
        assertEquals(WALLET_BALANCE, result.get().getUserWallet().getWalletValue());
        assertEquals(WALLET_BALANCE, result.get().getUserWallet().getBalanceAvailable());
    }

    @Test
    void shouldNotFindUser() {
        final Optional<User> result = userService.getUser(USERNAME);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "password", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        Mockito.when(userDAOMock.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        userService.deleteUser(USERNAME);

        Mockito.verify(userDAOMock, Mockito.times(1)).deleteUser(user);
    }

    @Test
    void shouldValidateAndRegisterUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);

        final UserDTO result = userService.validateAndRegisterUser(userRegisterDTO);

        Mockito.verify(userDAOMock, Mockito.times(1)).saveUser(Mockito.any(User.class));
        assertEquals(USERNAME, result.username());
        assertEquals(EMAIL, result.email());
        assertEquals(BigDecimal.ZERO, result.userWallet().stockValue());
        assertEquals(WALLET_BALANCE, result.userWallet().balanceAvailable());
        assertEquals(WALLET_BALANCE, result.userWallet().walletValue());
        assertEquals(WALLET_BALANCE, result.userWallet().previousWalletValue());
        assertEquals(BigDecimal.ZERO, result.userWallet().walletPercentageChange());
        assertEquals(0, result.userWallet().userStocks().size());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        Mockito.when(userDAOMock.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertThrows(UserNameExistsException.class, () -> userService.validateAndRegisterUser(userRegisterDTO));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        Mockito.when(userDAOMock.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(UserEmailExistsException.class, () -> userService.validateAndRegisterUser(userRegisterDTO));
    }

}