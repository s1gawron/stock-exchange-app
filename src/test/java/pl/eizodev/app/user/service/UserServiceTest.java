package pl.eizodev.app.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.eizodev.app.user.dto.UserDTO;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.exception.UserEmailExistsException;
import pl.eizodev.app.user.exception.UserNameExistsException;
import pl.eizodev.app.user.model.User;
import pl.eizodev.app.user.model.UserRole;
import pl.eizodev.app.user.model.UserWallet;
import pl.eizodev.app.user.repository.UserRepository;

import java.math.BigDecimal;
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
        assertEquals(UserRole.USER, result.get().getRole());
        assertEquals(0, result.get().getUserWallet().getUserStock().size());
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
        assertEquals(USERNAME, result.getUsername());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(BigDecimal.ZERO, result.getUserWallet().getStockValue());
        assertEquals(WALLET_BALANCE, result.getUserWallet().getBalanceAvailable());
        assertEquals(WALLET_BALANCE, result.getUserWallet().getWalletValue());
        assertEquals(WALLET_BALANCE, result.getUserWallet().getPreviousWalletValue());
        assertEquals(BigDecimal.ZERO, result.getUserWallet().getWalletPercentageChange());
        assertEquals(0, result.getUserWallet().getUserStock().size());
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

}