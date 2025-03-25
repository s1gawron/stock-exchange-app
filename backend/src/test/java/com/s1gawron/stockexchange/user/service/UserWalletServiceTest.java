package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.exception.UserUnauthenticatedException;
import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.InMemoryStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.dao.impl.InMemoryUserWalletDAO;
import com.s1gawron.stockexchange.user.dto.UserStockDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserStock;
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
import java.util.Optional;

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
    void shouldUpdateAndGetUserWalletWithNoStock() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        userWalletDAO.saveUserWallet(userWallet);

        final UserWalletDTO result = userWalletService.updateAndGetUserWalletDTO();

        assertEquals(BigDecimal.ZERO, result.stockValue());
        assertEquals(USER_BALANCE_AVAILABLE, result.balanceAvailable());
        assertEquals(BigDecimal.ZERO, result.balanceBlocked());
        assertEquals(USER_BALANCE_AVAILABLE, result.value());
        assertEquals(PREVIOUS_USER_WALLET_VALUE, result.lastDayValue());
        assertEquals(new BigDecimal("-42.20"), result.valuePercentageChange());
        assertEquals(LocalDateTime.now(CLOCK), result.lastUpdateDate());
    }

    @Test
    void shouldUpdateAndGetUserWalletWithStock() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(appleStock);

        final StockDataDTO amazonStock = StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(amazonStock);

        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);
        UserStockGeneratorHelper.I.getUserStocks(WALLET_ID).forEach(stock -> userWalletDAO.saveUserStock(stock));

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        final UserWalletDTO result = userWalletService.updateAndGetUserWalletDTO();

        assertEquals(new BigDecimal("7500.00"), result.stockValue());
        assertEquals(USER_BALANCE_AVAILABLE, result.balanceAvailable());
        assertEquals(BigDecimal.ZERO, result.balanceBlocked());
        assertEquals(new BigDecimal("17500.00"), result.value());
        assertEquals(PREVIOUS_USER_WALLET_VALUE, result.lastDayValue());
        assertEquals(new BigDecimal("1.16"), result.valuePercentageChange());
        assertEquals(LocalDateTime.now(CLOCK), result.lastUpdateDate());
    }

    @Test
    void shouldUpdateAndGetUserWalletWithBlockedStock() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(appleStock);

        final BigDecimal previousWalletValue = new BigDecimal("10000.00");
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, previousWalletValue);
        final UserStock userStock = UserStockGeneratorHelper.I.getAppleUserStock(WALLET_ID, 20, 10, new BigDecimal("25.00"));

        userWalletDAO.saveUserWallet(userWallet);
        userWalletDAO.saveUserStock(userStock);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        final UserWalletDTO result = userWalletService.updateAndGetUserWalletDTO();

        assertEquals(new BigDecimal("600.00"), result.stockValue());
        assertEquals(USER_BALANCE_AVAILABLE, result.balanceAvailable());
        assertEquals(BigDecimal.ZERO, result.balanceBlocked());
        assertEquals(new BigDecimal("10600.00"), result.value());
        assertEquals(previousWalletValue, result.lastDayValue());
        assertEquals(new BigDecimal("6.00"), result.valuePercentageChange());
        assertEquals(LocalDateTime.now(CLOCK), result.lastUpdateDate());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticatedWhileGettingWallet() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        userWalletDAO.saveUserWallet(userWallet);

        assertThrows(UserUnauthenticatedException.class, () -> userWalletService.updateAndGetUserWalletDTO());
    }

    @Test
    void shouldThrowExceptionWhenUserWalletIsNotFoundWhileGettingWallet() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        assertThrows(UserWalletNotFoundException.class, () -> userWalletService.updateAndGetUserWalletDTO());
    }

    @Test
    void shouldUpdateUserWalletWithNoStockAtTheEndOfTheDay() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);

        userWalletService.updateWalletAtTheEndOfTheDay(WALLET_ID);

        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getLastDayValue());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastDayUpdateDate());
    }

    @Test
    void shouldUpdateUserWalletWithStockAtTheEndOfTheDay() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(appleStock);

        final StockDataDTO amazonStock = StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(amazonStock);

        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);
        UserStockGeneratorHelper.I.getUserStocks(WALLET_ID).forEach(stock -> userWalletDAO.saveUserStock(stock));

        userWalletService.updateWalletAtTheEndOfTheDay(WALLET_ID);

        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("17500.00"), userWallet.getLastDayValue());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastDayUpdateDate());
    }

    @Test
    void shouldGetUserStocks() {
        final StockDataDTO appleStock = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(appleStock);

        final StockDataDTO amazonStock = StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00"));
        stockDataProvider.addStockData(amazonStock);

        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);
        UserStockGeneratorHelper.I.getUserStocks(WALLET_ID).forEach(stock -> userWalletDAO.saveUserStock(stock));

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        final List<UserStockDTO> result = userWalletService.getUserStocks();

        assertEquals(2, result.size());
        assertUserStock(AAPL_TICKER, result, appleStock, 100, new BigDecimal("25.00"), new BigDecimal("500.00"));
        assertUserStock(AMZN_TICKER, result, amazonStock, 150, new BigDecimal("32.00"), new BigDecimal("-300.00"));
    }

    @Test
    void shouldReturnEmptyListWhenUserHaveNoStocks() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        final List<UserStockDTO> result = userWalletService.getUserStocks();

        assertEquals(0, result.size());
    }

    @Test
    void shouldNotGetUserStocksForUnauthenticatedUser() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);
        userWalletDAO.saveUserWallet(userWallet);

        assertThrows(UserUnauthenticatedException.class, () -> userWalletService.getUserStocks());
    }

    @Test
    void shouldThrowExceptionWhenUserWalletIsNotFoundWhileGettingUserStocks() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        assertThrows(UserWalletNotFoundException.class, () -> userWalletService.getUserStocks());
    }

    public void assertUserStock(final String ticker, final List<UserStockDTO> result, final StockDataDTO stockData, final int expectedQuantity,
        final BigDecimal expectedAveragePurchasePrice, final BigDecimal expectedProfitLoss) {
        final Optional<UserStockDTO> userStock = result.stream()
            .filter(el -> el.ticker().equals(ticker))
            .findFirst();

        assertTrue(userStock.isPresent());
        assertEquals(userStock.get().ticker(), stockData.ticker());
        assertEquals(userStock.get().name(), stockData.companyFullName());
        assertEquals(userStock.get().price(), stockData.stockQuote().currentPrice());
        assertEquals(userStock.get().priceChange(), stockData.stockQuote().priceChange());
        assertEquals(userStock.get().percentagePriceChange(), stockData.stockQuote().percentagePriceChange());
        assertEquals(userStock.get().quantity(), expectedQuantity);
        assertEquals(userStock.get().averagePurchasePrice(), expectedAveragePurchasePrice);
        assertEquals(userStock.get().profitLoss(), expectedProfitLoss);
    }

}