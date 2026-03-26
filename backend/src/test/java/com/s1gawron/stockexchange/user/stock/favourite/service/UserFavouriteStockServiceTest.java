package com.s1gawron.stockexchange.user.stock.favourite.service;

import com.s1gawron.stockexchange.shared.exception.UserUnauthenticatedException;
import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.stock.favourite.dao.impl.InMemoryUserFavouriteStockDAO;
import com.s1gawron.stockexchange.user.stock.favourite.dto.AddFavouriteStockRequestDTO;
import com.s1gawron.stockexchange.user.stock.favourite.dto.UserFavouriteStockDTO;
import com.s1gawron.stockexchange.user.stock.favourite.exception.UserFavouriteStockAlreadyExistsException;
import com.s1gawron.stockexchange.user.stock.favourite.exception.UserFavouriteStockNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserFavouriteStockServiceTest {

    private static final long USER_ID = 1L;

    private static final String USERNAME = "testUser";

    private static final User USER = UserCreatorHelper.I.createUser(USER_ID, USERNAME);

    private static final String AAPL_TICKER = "AAPL";

    private static final String AMZN_TICKER = "AMZN";

    private Authentication authenticationMock;

    private InMemoryUserFavouriteStockDAO userFavouriteStockDAO;

    private UserFavouriteStockService userFavouriteStockService;

    @BeforeEach
    void setUp() {
        authenticationMock = Mockito.mock(Authentication.class);
        final SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContextMock.getAuthentication()).thenReturn(authenticationMock);
        SecurityContextHolder.setContext(securityContextMock);

        userFavouriteStockDAO = new InMemoryUserFavouriteStockDAO();
        userFavouriteStockService = new UserFavouriteStockService(userFavouriteStockDAO);
    }

    @Test
    void shouldAddFavouriteStock() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        final UserFavouriteStockDTO result = userFavouriteStockService.addFavouriteStock(new AddFavouriteStockRequestDTO(AAPL_TICKER));

        assertNotNull(result.id());
        assertEquals(AAPL_TICKER, result.ticker());
    }

    @Test
    void shouldThrowExceptionWhenAddingDuplicateFavouriteStock() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        userFavouriteStockService.addFavouriteStock(new AddFavouriteStockRequestDTO(AAPL_TICKER));

        assertThrows(UserFavouriteStockAlreadyExistsException.class,
            () -> userFavouriteStockService.addFavouriteStock(new AddFavouriteStockRequestDTO(AAPL_TICKER)));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticatedWhileAddingFavourite() {
        assertThrows(UserUnauthenticatedException.class,
            () -> userFavouriteStockService.addFavouriteStock(new AddFavouriteStockRequestDTO(AAPL_TICKER)));
    }

    @Test
    void shouldRemoveFavouriteStock() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        userFavouriteStockService.addFavouriteStock(new AddFavouriteStockRequestDTO(AAPL_TICKER));
        userFavouriteStockService.removeFavouriteStock(AAPL_TICKER);

        final List<UserFavouriteStockDTO> result = userFavouriteStockService.getUserFavouriteStocks();
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentFavouriteStock() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        assertThrows(UserFavouriteStockNotFoundException.class,
            () -> userFavouriteStockService.removeFavouriteStock(AAPL_TICKER));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticatedWhileRemovingFavourite() {
        assertThrows(UserUnauthenticatedException.class,
            () -> userFavouriteStockService.removeFavouriteStock(AAPL_TICKER));
    }

    @Test
    void shouldGetUserFavouriteStocks() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        userFavouriteStockService.addFavouriteStock(new AddFavouriteStockRequestDTO(AAPL_TICKER));
        userFavouriteStockService.addFavouriteStock(new AddFavouriteStockRequestDTO(AMZN_TICKER));

        final List<UserFavouriteStockDTO> result = userFavouriteStockService.getUserFavouriteStocks();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.ticker().equals(AAPL_TICKER)));
        assertTrue(result.stream().anyMatch(dto -> dto.ticker().equals(AMZN_TICKER)));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFavourites() {
        Mockito.when(authenticationMock.getPrincipal()).thenReturn(USER);

        final List<UserFavouriteStockDTO> result = userFavouriteStockService.getUserFavouriteStocks();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticatedWhileGettingFavourites() {
        assertThrows(UserUnauthenticatedException.class,
            () -> userFavouriteStockService.getUserFavouriteStocks());
    }
}
