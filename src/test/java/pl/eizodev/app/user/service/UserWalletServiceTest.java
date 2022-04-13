package pl.eizodev.app.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.eizodev.app.stock.dataprovider.StockDataProvider;
import pl.eizodev.app.stock.dataprovider.dto.StockDataDTO;
import pl.eizodev.app.stock.dataprovider.dto.StockQuoteDTO;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.dto.UserWalletDTO;
import pl.eizodev.app.user.exception.UserWalletNotFoundException;
import pl.eizodev.app.user.model.User;
import pl.eizodev.app.user.model.UserStock;
import pl.eizodev.app.user.model.UserWallet;
import pl.eizodev.app.user.repository.UserWalletRepository;

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

    private static final BigDecimal USER_WALLET_BALANCE = new BigDecimal("10000.00");

    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private UserWalletRepository userWalletRepositoryMock;

    private StockDataProvider stockDataProviderMock;

    private UserWalletService userWalletService;

    private UserWallet userWallet;

    @BeforeEach
    void setUp() {
        userWalletRepositoryMock = Mockito.mock(UserWalletRepository.class);
        stockDataProviderMock = Mockito.mock(StockDataProvider.class);

        userWalletService = new UserWalletService(userWalletRepositoryMock, stockDataProviderMock, CLOCK);

        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO(USERNAME, "test@test.pl", "password", USER_WALLET_BALANCE);
        final User user = User.createUser(userRegisterDTO, "encryptedPassword");
        userWallet = UserWallet.createNewUserWallet(user, USER_WALLET_BALANCE);
    }

    @Test
    void shouldUpdateAndGetUserWalletWithNoStock() {
        final BigDecimal previousWalletValue = new BigDecimal("15000.00");

        userWallet.setPreviousWalletValue(previousWalletValue);
        userWallet.setLastUpdateDate(LocalDateTime.of(2022, 4, 11, 17, 56, 22));
        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME)).thenReturn(Optional.of(userWallet));

        final UserWalletDTO result = userWalletService.updateAndGetUserWallet(USERNAME);

        assertEquals(BigDecimal.ZERO, result.getStockValue());
        assertEquals(USER_WALLET_BALANCE, result.getBalanceAvailable());
        assertEquals(USER_WALLET_BALANCE, result.getWalletValue());
        assertEquals(previousWalletValue, result.getPreviousWalletValue());
        assertEquals(new BigDecimal("-33.33"), result.getWalletPercentageChange());
        assertEquals(0, result.getUserStock().size());
        assertEquals(LocalDateTime.now(CLOCK), result.getLastUpdateDate());
    }

    @Test
    void shouldUpdateAndGetUserWalletWithStock() {
        final BigDecimal previousWalletValue = new BigDecimal("17300.00");
        final BigDecimal appleStockAveragePuchasePrice = new BigDecimal("25.00");
        final BigDecimal amazonStockAveragePuchasePrice = new BigDecimal("32.00");
        final List<UserStock> userStockList = List.of(
            new UserStock(userWallet, "AAPL", appleStockAveragePuchasePrice, 100),
            new UserStock(userWallet, "AMZN", amazonStockAveragePuchasePrice, 150)
        );

        final StockQuoteDTO appleStockQuote = new StockQuoteDTO("USD", new BigDecimal("30.00"), new BigDecimal("5.00"), new BigDecimal("20.00"),
            new BigDecimal("32.00"), new BigDecimal("25.00"), new BigDecimal("26.00"), new BigDecimal("27.00"));
        final StockDataDTO appleStock = new StockDataDTO("AAPL", "Apple Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "Technology", "1980-12-12",
            BigDecimal.valueOf(2530982), 16319.44, appleStockQuote, "2022-03-17T21:00:04");
        final StockQuoteDTO amazonStockQuote = new StockQuoteDTO("USD", new BigDecimal("30.00"), new BigDecimal("-2.00"), new BigDecimal("7.00"),
            new BigDecimal("32.00"), new BigDecimal("25.00"), new BigDecimal("26.00"), new BigDecimal("27.00"));
        final StockDataDTO amazonStock = new StockDataDTO("AMZN", "Amazon Inc", "US", "NASDAQ NMS - GLOBAL MARKET", "E-Commerce", "1980-12-12",
            BigDecimal.valueOf(2530982), 16319.44, amazonStockQuote, "2022-03-17T21:00:04");

        userWallet.setUserStock(userStockList);
        userWallet.setPreviousWalletValue(previousWalletValue);
        userWallet.setLastUpdateDate(LocalDateTime.of(2022, 4, 11, 17, 56, 22));
        Mockito.when(userWalletRepositoryMock.findByUser_Username(USERNAME)).thenReturn(Optional.of(userWallet));
        Mockito.when(stockDataProviderMock.getStockData("AAPL")).thenReturn(appleStock);
        Mockito.when(stockDataProviderMock.getStockData("AMZN")).thenReturn(amazonStock);

        final UserWalletDTO result = userWalletService.updateAndGetUserWallet(USERNAME);

        assertEquals(new BigDecimal("7500.00"), result.getStockValue());
        assertEquals(USER_WALLET_BALANCE, result.getBalanceAvailable());
        assertEquals(new BigDecimal("17500.00"), result.getWalletValue());
        assertEquals(previousWalletValue, result.getPreviousWalletValue());
        assertEquals(new BigDecimal("1.16"), result.getWalletPercentageChange());
        assertEquals(2, result.getUserStock().size());
        assertEquals(appleStockAveragePuchasePrice, result.getUserStock().get(0).getAveragePurchasePrice());
        assertEquals(amazonStockAveragePuchasePrice, result.getUserStock().get(1).getAveragePurchasePrice());
        assertEquals(LocalDateTime.now(CLOCK), result.getLastUpdateDate());
    }

    @Test
    void shouldThrowExceptionWhenUserWalletIsNotFound() {
        Assertions.assertThrows(UserWalletNotFoundException.class, () -> userWalletService.updateAndGetUserWallet(USERNAME));
    }

}