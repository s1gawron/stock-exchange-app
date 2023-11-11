package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserWalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserWalletServiceTest {

    private static final String USERNAME = "user";

    private static final String USERNAME_1 = "user1";

    private static final String USERNAME_2 = "user2";

    private static final BigDecimal USER_BALANCE_AVAILABLE = new BigDecimal("10000.00");

    private static final BigDecimal USER_BALANCE_AVAILABLE_1 = new BigDecimal("12500.00");

    private static final BigDecimal USER_BALANCE_AVAILABLE_2 = new BigDecimal("9855.00");

    private static final BigDecimal PREVIOUS_USER_WALLET_VALUE = new BigDecimal("17300.00");

    private static final BigDecimal PREVIOUS_USER_WALLET_VALUE_1 = new BigDecimal("12500.00");

    private static final BigDecimal PREVIOUS_USER_WALLET_VALUE_2 = new BigDecimal("5000.00");

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private UserWalletRepository userWalletRepositoryMock;

    private StockDataProvider stockDataProviderMock;

    private UserWalletService userWalletService;

    @BeforeEach
    void setUp() {
        userWalletRepositoryMock = Mockito.mock(UserWalletRepository.class);
        stockDataProviderMock = Mockito.mock(StockDataProvider.class);
        userWalletService = new UserWalletService(userWalletRepositoryMock, stockDataProviderMock, CLOCK);
    }

    @Test
    void shouldUpdateAndGetUserWalletWithNoStock() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWalletWithNoStock(USERNAME, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME)).thenReturn(Optional.of(userWallet));

        userWalletService.updateAndGetUserWallet(USERNAME);

        assertUserWallet(List.of(userWallet), USERNAME, BigDecimal.ZERO, USER_BALANCE_AVAILABLE, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE,
            new BigDecimal("-42.20"), 0);
    }

    @Test
    void shouldUpdateAndGetUserWalletWithStock() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWalletWithStock(USERNAME, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME)).thenReturn(Optional.of(userWallet));
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00")));
        Mockito.when(stockDataProviderMock.getStockData("AMZN")).thenReturn(StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00")));

        userWalletService.updateAndGetUserWallet(USERNAME);

        assertUserWallet(List.of(userWallet), USERNAME, new BigDecimal("7500.00"), USER_BALANCE_AVAILABLE, new BigDecimal("17500.00"),
            PREVIOUS_USER_WALLET_VALUE, new BigDecimal("1.16"), 2);
    }

    @Test
    void shouldUpdateUserWalletsWithNoStockAtTheEndOfTheDay() {
        final List<BigDecimal> availableBalances = List.of(USER_BALANCE_AVAILABLE, USER_BALANCE_AVAILABLE_1, USER_BALANCE_AVAILABLE_2);
        final List<BigDecimal> previousWalletValues = List.of(PREVIOUS_USER_WALLET_VALUE, PREVIOUS_USER_WALLET_VALUE_1, PREVIOUS_USER_WALLET_VALUE_2);
        final List<UserWallet> userWallets = UserWalletGeneratorHelper.I.getUserWalletsWithNoStock(availableBalances, previousWalletValues);

        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME)).thenReturn(Optional.of(userWallets.get(0)));
        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME_1)).thenReturn(Optional.of(userWallets.get(1)));
        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME_2)).thenReturn(Optional.of(userWallets.get(2)));

        userWalletService.updateUserWalletsAtTheEndOfTheDay(List.of(USERNAME, USERNAME_1, USERNAME_2));

        assertUserWallet(userWallets, USERNAME, BigDecimal.ZERO, USER_BALANCE_AVAILABLE, USER_BALANCE_AVAILABLE, USER_BALANCE_AVAILABLE, BigDecimal.ZERO, 0);
        assertUserWallet(userWallets, USERNAME_1, BigDecimal.ZERO, USER_BALANCE_AVAILABLE_1, USER_BALANCE_AVAILABLE_1, USER_BALANCE_AVAILABLE_1,
            BigDecimal.ZERO, 0);
        assertUserWallet(userWallets, USERNAME_2, BigDecimal.ZERO, USER_BALANCE_AVAILABLE_2, USER_BALANCE_AVAILABLE_2, USER_BALANCE_AVAILABLE_2,
            BigDecimal.ZERO, 0);
    }

    @Test
    void shouldUpdateUserWalletsWithStockAtTheEndOfTheDay() {
        final List<BigDecimal> availableBalances = List.of(USER_BALANCE_AVAILABLE, USER_BALANCE_AVAILABLE_1, USER_BALANCE_AVAILABLE_2);
        final List<BigDecimal> previousWalletValues = List.of(PREVIOUS_USER_WALLET_VALUE, PREVIOUS_USER_WALLET_VALUE_1, PREVIOUS_USER_WALLET_VALUE_2);
        final List<UserWallet> userWallets = UserWalletGeneratorHelper.I.getUserWalletsWithStock(availableBalances, previousWalletValues);

        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME)).thenReturn(Optional.of(userWallets.get(0)));
        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME_1)).thenReturn(Optional.of(userWallets.get(1)));
        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME_2)).thenReturn(Optional.of(userWallets.get(2)));
        mockStockWithDifferentPrices();

        userWalletService.updateUserWalletsAtTheEndOfTheDay(List.of(USERNAME, USERNAME_1, USERNAME_2));

        assertUserWallet(userWallets, USERNAME, new BigDecimal("7500.00"), USER_BALANCE_AVAILABLE, new BigDecimal("17500.00"), new BigDecimal("17500.00"),
            BigDecimal.ZERO, 2);
        assertUserWallet(userWallets, USERNAME_1, new BigDecimal("6250.00"), USER_BALANCE_AVAILABLE_1, new BigDecimal("18750.00"), new BigDecimal("18750.00"),
            BigDecimal.ZERO, 2);
        assertUserWallet(userWallets, USERNAME_2, new BigDecimal("3750.00"), USER_BALANCE_AVAILABLE_2, new BigDecimal("13605.00"), new BigDecimal("13605.00"),
            BigDecimal.ZERO, 2);
    }

    private void mockStockWithDifferentPrices() {
        final StockDataDTO firstAppleStockInvoke = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        final StockDataDTO secondAppleStockInvoke = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("25.00"));
        final StockDataDTO thirdAppleStockInvoke = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("15.00"));
        final StockDataDTO firstAmazonStockInvoke = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        final StockDataDTO secondAmazonStockInvoke = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("25.00"));
        final StockDataDTO thirdAmazonStockInvoke = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("15.00"));

        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(firstAppleStockInvoke, secondAppleStockInvoke, thirdAppleStockInvoke);
        Mockito.when(stockDataProviderMock.getStockData("AMZN")).thenReturn(firstAmazonStockInvoke, secondAmazonStockInvoke, thirdAmazonStockInvoke);
    }

    private void assertUserWallet(final List<UserWallet> results, final String username, final BigDecimal expectedStockValue,
        final BigDecimal expectedBalanceAvailable, final BigDecimal expectedWalletValue, final BigDecimal expectedPreviousWalletValue,
        final BigDecimal expectedWalletPercentageChange, final int expectedUserStockListSize) {
        final UserWallet result = results.stream()
            .filter(wallet -> wallet.getUser().getUsername().equals(username))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("User wallet not found!"));

        assertEquals(expectedStockValue, result.getStockValue());
        assertEquals(expectedBalanceAvailable, result.getBalanceAvailable());
        assertEquals(expectedWalletValue, result.getWalletValue());
        assertEquals(expectedPreviousWalletValue, result.getPreviousWalletValue());
        assertEquals(expectedWalletPercentageChange, result.getWalletPercentageChange());
        assertEquals(expectedUserStockListSize, result.getUserStocks().size());
        assertEquals(LocalDateTime.now(CLOCK), result.getLastUpdateDate());
    }

    @Test
    void shouldThrowExceptionWhenUserWalletIsNotFound() {
        Assertions.assertThrows(UserWalletNotFoundException.class, () -> userWalletService.updateAndGetUserWallet(USERNAME));
    }

}