package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.exception.UserUnauthenticatedException;
import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserWalletDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserWalletServiceTest {

    private static final long USER_ID = 1;

    private static final String USERNAME = "testUser";

    private static final User USER = UserCreatorHelper.I.createUser(USER_ID, USERNAME);

    private static final BigDecimal USER_BALANCE_AVAILABLE = new BigDecimal("10000.00");

    private static final BigDecimal PREVIOUS_USER_WALLET_VALUE = new BigDecimal("17300.00");

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private Authentication authenticationMock;

    private UserWalletDAO userWalletDAOMock;

    private StockDataProvider stockDataProviderMock;

    private UserWalletService userWalletService;

    @BeforeEach
    void setUp() {
        authenticationMock = Mockito.mock(Authentication.class);
        final SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        userWalletDAOMock = Mockito.mock(UserWalletDAO.class);
        stockDataProviderMock = Mockito.mock(StockDataProvider.class);
        userWalletService = new UserWalletService(userWalletDAOMock, stockDataProviderMock, CLOCK);
    }

    @Test
    void shouldUpdateAndGetUserWalletWithNoStock() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWalletWithNoStock(USERNAME, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));

        final UserWalletDTO result = userWalletService.updateAndGetUserWallet();

        assertEquals(BigDecimal.ZERO, result.stockValue());
        assertEquals(USER_BALANCE_AVAILABLE, result.balanceAvailable());
        assertEquals(USER_BALANCE_AVAILABLE, result.walletValue());
        assertEquals(PREVIOUS_USER_WALLET_VALUE, result.previousWalletValue());
        assertEquals(new BigDecimal("-42.20"), result.walletPercentageChange());
        assertEquals(0, result.userStocks().size());
        assertEquals(LocalDateTime.now(CLOCK), result.lastUpdateDate());
    }

    @Test
    void shouldUpdateAndGetUserWalletWithStock() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWalletWithStock(USERNAME, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00")));
        Mockito.when(stockDataProviderMock.getStockData("AMZN")).thenReturn(StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00")));

        final UserWalletDTO result = userWalletService.updateAndGetUserWallet();

        assertEquals(new BigDecimal("7500.00"), result.stockValue());
        assertEquals(USER_BALANCE_AVAILABLE, result.balanceAvailable());
        assertEquals(new BigDecimal("17500.00"), result.walletValue());
        assertEquals(PREVIOUS_USER_WALLET_VALUE, result.previousWalletValue());
        assertEquals(new BigDecimal("1.16"), result.walletPercentageChange());
        assertEquals(2, result.userStocks().size());
        assertEquals(LocalDateTime.now(CLOCK), result.lastUpdateDate());
    }

    @Test
    void shouldNotUpdateAndGetUserWalletForUnauthenticatedUser() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWalletWithNoStock(USERNAME, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));

        Assertions.assertThrows(UserUnauthenticatedException.class, () -> userWalletService.updateAndGetUserWallet());
    }

    @Test
    void shouldUpdateUserWalletWithNoStockAtTheEndOfTheDay() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWalletWithNoStock(USERNAME, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));

        userWalletService.updateUserWalletAtTheEndOfTheDay(USER_ID);

        assertEquals(BigDecimal.ZERO, userWallet.getStockValue());
        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getWalletValue());
        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getPreviousWalletValue());
        assertEquals(BigDecimal.ZERO, userWallet.getWalletPercentageChange());
        assertEquals(0, userWallet.getUserStocks().size());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastUpdateDate());
    }

    @Test
    void shouldUpdateUserWalletWithStockAtTheEndOfTheDay() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWalletWithStock(USERNAME, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        mockStockWithDifferentPrices();

        userWalletService.updateUserWalletAtTheEndOfTheDay(USER_ID);

        assertEquals(new BigDecimal("7500.00"), userWallet.getStockValue());
        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(new BigDecimal("17500.00"), userWallet.getWalletValue());
        assertEquals(new BigDecimal("17500.00"), userWallet.getPreviousWalletValue());
        assertEquals(BigDecimal.ZERO, userWallet.getWalletPercentageChange());
        assertEquals(2, userWallet.getUserStocks().size());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastUpdateDate());
    }

    @Test
    void shouldThrowExceptionWhenUserWalletIsNotFound() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Assertions.assertThrows(UserWalletNotFoundException.class, () -> userWalletService.updateAndGetUserWallet());
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

}