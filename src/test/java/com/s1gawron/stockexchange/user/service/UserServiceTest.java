package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserRole;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static final String USERNAME = "testUser";

    private static final BigDecimal WALLET_BALANCE = new BigDecimal("10000.00");

    private static final String EMAIL = "test@test.pl";

    private UserRepository userRepositoryMock;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepositoryMock = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepositoryMock);
    }

    @Test
    void shouldFindUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "password", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");
        final UserWallet userWallet = UserWallet.createNewUserWallet(user, WALLET_BALANCE);
        user.setUserWallet(userWallet);

        Mockito.when(userRepositoryMock.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        final Optional<User> result = userService.getUser(USERNAME);

        assertTrue(result.isPresent());
        assertEquals(USERNAME, result.get().getUsername());
        Assertions.assertEquals(UserRole.USER, result.get().getRole());
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

        Mockito.when(userRepositoryMock.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        userService.deleteUser(USERNAME);

        Mockito.verify(userRepositoryMock, Mockito.times(1)).delete(user);
    }

    @Test
    void shouldValidateAndRegisterUser() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);

        final UserDTO result = userService.validateAndRegisterUser(userRegisterDTO);

        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(Mockito.any(User.class));
        assertEquals(USERNAME, result.username());
        assertEquals(EMAIL, result.email());
        assertEquals(BigDecimal.ZERO, result.userWallet().stockValue());
        assertEquals(WALLET_BALANCE, result.userWallet().balanceAvailable());
        assertEquals(WALLET_BALANCE, result.userWallet().walletValue());
        assertEquals(WALLET_BALANCE, result.userWallet().previousWalletValue());
        assertEquals(BigDecimal.ZERO, result.userWallet().walletPercentageChange());
        assertEquals(0, result.userWallet().userStock().size());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        Mockito.when(userRepositoryMock.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        assertThrows(UserNameExistsException.class, () -> userService.validateAndRegisterUser(userRegisterDTO));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");

        Mockito.when(userRepositoryMock.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertThrows(UserEmailExistsException.class, () -> userService.validateAndRegisterUser(userRegisterDTO));
    }

    @Test
    void shouldFindUsernames() {
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, EMAIL, "Start00!", WALLET_BALANCE);
        final UserRegisterDTO userRegisterDTO2 = new UserRegisterDTO("testUser2", "test2@test.pl", "Start00!", WALLET_BALANCE);
        final UserRegisterDTO userRegisterDTO3 = new UserRegisterDTO("testUser3", "test3@test.pl", "Start00!", WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");
        final User user2 = User.createUser(userRegisterDTO2, "encryptedPassword");
        final User user3 = User.createUser(userRegisterDTO3, "encryptedPassword");

        Mockito.when(userRepositoryMock.findAll()).thenReturn(List.of(user, user2, user3));

        final List<String> result = userService.getAllUsernames();
        final List<String> expectedList = List.of("testUser", "testUser2", "testUser3");

        assertEquals(3, result.size());
        assertEquals(expectedList, result);
    }

    @Test
    void shouldNotFindUsernames() {
        final List<String> result = userService.getAllUsernames();

        assertEquals(0, result.size());
    }

}