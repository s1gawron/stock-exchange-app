package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.InMemoryStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.dao.impl.InMemoryUserWalletDAO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserWallet;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserWalletServiceTest {

    private static final long USER_ID = 1L;

    public static final long WALLET_ID = 0L;

    private static final String USERNAME = "testUser";

    private static final User USER = UserCreatorHelper.I.createUser(USER_ID, USERNAME);

    private static final BigDecimal USER_BALANCE_AVAILABLE = new BigDecimal("10000.00");

    private static final BigDecimal PREVIOUS_USER_WALLET_VALUE = new BigDecimal("17300.00");

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private static final String AAPL_TICKER = "AAPL";

    private static final String AMZN_TICKER = "AMZN";

    private Authentication authenticationMock;

    private InMemoryUserWalletDAO userWalletDAO;

    private InMemoryStockDataProvider stockDataProvider;

    private UserWalletService userWalletService;

    @BeforeEach
    void setUp() {
        authenticationMock = Mockito.mock(Authentication.class);
        final SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        userWalletDAO = new InMemoryUserWalletDAO();
        stockDataProvider = new InMemoryStockDataProvider();
        userWalletService = new UserWalletService(userWalletDAO, stockDataProvider, CLOCK);
    }

    @Test
    void shouldThrowExceptionWhenUserWalletIsNotFoundWhileLockingWallet() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        assertThrows(UserWalletNotFoundException.class, () -> userWalletService.lockUserWallet());
    }

    @Test
    void shouldUpdateUserWalletWithNoStockAtTheEndOfTheDay() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);

        userWalletService.updateWalletAtTheEndOfTheDay(WALLET_ID, Map.of());

        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getLastDayValue());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastDayUpdateDate());
    }

    @Test
    void shouldUpdateUserWalletWithStockAtTheEndOfTheDayUsingGivenStockPrices() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);
        UserStockGeneratorHelper.I.getUserStockPositions(WALLET_ID).forEach(position -> userWalletDAO.saveUserStockPosition(position));

        userWalletService.updateWalletAtTheEndOfTheDay(WALLET_ID, Map.of(AAPL_TICKER, new BigDecimal("30.00"), AMZN_TICKER, new BigDecimal("30.00")));

        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("17500.0000"), userWallet.getLastDayValue());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastDayUpdateDate());
    }

    @Test
    void shouldFetchMissingStockPriceWhileUpdatingUserWalletAtTheEndOfTheDay() {
        final StockDataDTO amazonStock = StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(amazonStock);

        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);
        UserStockGeneratorHelper.I.getUserStockPositions(WALLET_ID).forEach(position -> userWalletDAO.saveUserStockPosition(position));

        userWalletService.updateWalletAtTheEndOfTheDay(WALLET_ID, Map.of(AAPL_TICKER, new BigDecimal("30.00")));

        assertEquals(new BigDecimal("17500.0000"), userWallet.getLastDayValue());
    }

    @Test
    void shouldGetUserStockTickersAndTheirPrices() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(appleStock);

        final StockDataDTO amazonStock = StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("40.00"));
        stockDataProvider.addStockData(amazonStock);

        UserStockGeneratorHelper.I.getUserStockPositions(WALLET_ID).forEach(position -> userWalletDAO.saveUserStockPosition(position));

        final List<String> tickers = userWalletService.getUserStockTickers(WALLET_ID);
        final Map<String, BigDecimal> result = userWalletService.getStockPrices(tickers);

        assertEquals(2, tickers.size());
        assertEquals(Map.of(AAPL_TICKER, new BigDecimal("30.00"), AMZN_TICKER, new BigDecimal("40.00")), result);
    }

}
