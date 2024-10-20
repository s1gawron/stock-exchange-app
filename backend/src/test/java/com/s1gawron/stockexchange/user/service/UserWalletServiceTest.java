package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.shared.exception.UserUnauthenticatedException;
import com.s1gawron.stockexchange.shared.helper.StockDataGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.shared.helper.UserStockGeneratorHelper;
import com.s1gawron.stockexchange.shared.helper.UserWalletGeneratorHelper;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.dto.UserStockDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserStock;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
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

    private static final long USER_ID = 1;

    private static final String USERNAME = "testUser";

    private static final User USER = UserCreatorHelper.I.createUser(USER_ID, USERNAME);

    private static final BigDecimal USER_BALANCE_AVAILABLE = new BigDecimal("10000.00");

    private static final BigDecimal PREVIOUS_USER_WALLET_VALUE = new BigDecimal("17300.00");

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private static final String AAPL_TICKER = "AAPL";

    private static final String AMZN_TICKER = "AMZN";

    private Authentication authenticationMock;

    private UserWalletDAO userWalletDAOMock;

    private FinnhubStockDataProvider finnhubStockDataProviderMock;

    private UserWalletService userWalletService;

    @BeforeEach
    void setUp() {
        authenticationMock = Mockito.mock(Authentication.class);
        final SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        userWalletDAOMock = Mockito.mock(UserWalletDAO.class);
        finnhubStockDataProviderMock = Mockito.mock(FinnhubStockDataProvider.class);
        userWalletService = new UserWalletService(userWalletDAOMock, finnhubStockDataProviderMock, CLOCK);
    }

    @Test
    void shouldUpdateAndGetUserWalletWithNoStock() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));

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
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        Mockito.when(userWalletDAOMock.getUserStocks(userWallet.getWalletId())).thenReturn(UserStockGeneratorHelper.I.getUserStocks(USER_ID));
        Mockito.when(finnhubStockDataProviderMock.getStockData(AAPL_TICKER)).thenReturn(StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00")));
        Mockito.when(finnhubStockDataProviderMock.getStockData(AMZN_TICKER)).thenReturn(StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00")));

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
        final BigDecimal previousWalletValue = new BigDecimal("10000.00");
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, previousWalletValue);
        final List<UserStock> userStock = List.of(UserStockGeneratorHelper.I.getAppleUserStock(USER_ID, 20, 10, new BigDecimal("25.00")));

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        Mockito.when(userWalletDAOMock.getUserStocks(userWallet.getWalletId())).thenReturn(userStock);
        Mockito.when(finnhubStockDataProviderMock.getStockData(AAPL_TICKER)).thenReturn(StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00")));

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

        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));

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

        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));

        userWalletService.updateUserWalletAtTheEndOfTheDay(USER_ID);

        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getLastDayValue());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastUpdateDate());
    }

    @Test
    void shouldUpdateUserWalletWithStockAtTheEndOfTheDay() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        Mockito.when(userWalletDAOMock.getUserStocks(userWallet.getWalletId())).thenReturn(UserStockGeneratorHelper.I.getUserStocks(USER_ID));
        Mockito.when(finnhubStockDataProviderMock.getStockData(AAPL_TICKER)).thenReturn(StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00")));
        Mockito.when(finnhubStockDataProviderMock.getStockData(AMZN_TICKER)).thenReturn(StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00")));

        userWalletService.updateUserWalletAtTheEndOfTheDay(USER_ID);

        assertEquals(USER_BALANCE_AVAILABLE, userWallet.getBalanceAvailable());
        assertEquals(BigDecimal.ZERO, userWallet.getBalanceBlocked());
        assertEquals(new BigDecimal("17500.00"), userWallet.getLastDayValue());
        assertEquals(LocalDateTime.now(CLOCK), userWallet.getLastUpdateDate());
    }

    @Test
    void shouldGetUserStocks() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        Mockito.when(userWalletDAOMock.getUserStocks(userWallet.getWalletId())).thenReturn(UserStockGeneratorHelper.I.getUserStocks(USER_ID));

        final StockDataDTO appleStockData = StockDataGeneratorHelper.I.getAppleStock(new BigDecimal("30.00"));
        Mockito.when(finnhubStockDataProviderMock.getStockData(AAPL_TICKER)).thenReturn(appleStockData);
        final StockDataDTO amazonStockData = StockDataGeneratorHelper.I.getAmazonStock(new BigDecimal("30.00"));
        Mockito.when(finnhubStockDataProviderMock.getStockData(AMZN_TICKER)).thenReturn(amazonStockData);

        final List<UserStockDTO> result = userWalletService.getUserStocks();

        assertEquals(2, result.size());
        assertUserStock(AAPL_TICKER, result, appleStockData, 100, new BigDecimal("25.00"), new BigDecimal("500.00"));
        assertUserStock(AMZN_TICKER, result, amazonStockData, 150, new BigDecimal("32.00"), new BigDecimal("-300.00"));
    }

    @Test
    void shouldReturnEmptyListWhenUserHaveNoStocks() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);
        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));
        Mockito.when(userWalletDAOMock.getUserStocks(userWallet.getWalletId())).thenReturn(List.of());

        final List<UserStockDTO> result = userWalletService.getUserStocks();

        assertEquals(0, result.size());
    }

    @Test
    void shouldNotGetUserStocksForUnauthenticatedUser() {
        final UserWallet userWallet = UserWalletGeneratorHelper.I.getUserWallet(USER_ID, USER_BALANCE_AVAILABLE, PREVIOUS_USER_WALLET_VALUE);

        Mockito.when(userWalletDAOMock.findUserWalletByUserId(USER_ID)).thenReturn(Optional.of(userWallet));

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