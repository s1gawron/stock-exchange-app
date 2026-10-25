package com.s1gawron.stockexchange.shared;

import com.s1gawron.stockexchange.stock.controller.StockControllerV2;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.exception.FinnhubConnectionFailedException;
import com.s1gawron.stockexchange.transaction.controller.TransactionController;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.service.TransactionService;
import com.s1gawron.stockexchange.user.controller.UserControllerV2;
import com.s1gawron.stockexchange.user.controller.UserPublicController;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.exception.UserWalletNotFoundException;
import com.s1gawron.stockexchange.user.service.UserAuthenticationService;
import com.s1gawron.stockexchange.user.service.UserService;
import com.s1gawron.stockexchange.user.service.UserWalletDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AbstractErrorHandlerControllerTest {

    private static final String TRANSACTION_CREATE_URL = "/api/transaction/v1/create";

    private static final String TRANSACTION_REQUEST_JSON = """
        {"type": "PURCHASE", "stockTicker": "AAPL", "price": 20.25, "quantity": 10}
        """;

    private static final String USER_STOCKS_URL = "/api/user/v2/wallet/stocks";

    private static final String STOCK_DATA_URL = "/api/public/stock/v2/AAPL";

    private static final String USER_REGISTER_URL = "/api/public/user/v1/register";

    private static final String USER_REGISTER_JSON = """
        {"username": "test", "email": "test@test.pl", "password": "Start00!"}
        """;

    private TransactionService transactionServiceMock;

    private UserWalletDetailsService userWalletDetailsServiceMock;

    private UserService userServiceMock;

    private FinnhubStockDataProvider finnhubStockDataProviderMock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        transactionServiceMock = Mockito.mock(TransactionService.class);
        userWalletDetailsServiceMock = Mockito.mock(UserWalletDetailsService.class);
        userServiceMock = Mockito.mock(UserService.class);
        finnhubStockDataProviderMock = Mockito.mock(FinnhubStockDataProvider.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(transactionServiceMock), new UserControllerV2(userWalletDetailsServiceMock),
                new UserPublicController(userServiceMock, Mockito.mock(UserAuthenticationService.class)),
                new StockControllerV2(finnhubStockDataProviderMock))
            .build();
    }

    @Test
    void shouldReturnBadGatewayWhenFinnhubConnectionFailsDuringTransaction() throws Exception {
        Mockito.doThrow(FinnhubConnectionFailedException.create(429)).when(transactionServiceMock)
            .validateCreateAndProcessTransaction(Mockito.any(TransactionRequestDTO.class));

        mockMvc.perform(post(TRANSACTION_CREATE_URL).contentType(MediaType.APPLICATION_JSON).content(TRANSACTION_REQUEST_JSON))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value(502))
            .andExpect(jsonPath("$.URI").value(TRANSACTION_CREATE_URL));
    }

    @Test
    void shouldReturnNotFoundWhenUserWalletIsNotFoundDuringTransaction() throws Exception {
        Mockito.doThrow(UserWalletNotFoundException.createForUser(1L)).when(transactionServiceMock)
            .validateCreateAndProcessTransaction(Mockito.any(TransactionRequestDTO.class));

        mockMvc.perform(post(TRANSACTION_CREATE_URL).contentType(MediaType.APPLICATION_JSON).content(TRANSACTION_REQUEST_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void shouldKeepControllerSpecificStatusWhenStockIsNotFoundDuringTransaction() throws Exception {
        Mockito.doThrow(StockNotFoundException.createFromTicker("AAPL")).when(transactionServiceMock)
            .validateCreateAndProcessTransaction(Mockito.any(TransactionRequestDTO.class));

        mockMvc.perform(post(TRANSACTION_CREATE_URL).contentType(MediaType.APPLICATION_JSON).content(TRANSACTION_REQUEST_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldReturnNotFoundWhenStockIsNotFoundWhileGettingUserStocks() throws Exception {
        Mockito.when(userWalletDetailsServiceMock.getUserStocks()).thenThrow(StockNotFoundException.createFromTicker("AAPL"));

        mockMvc.perform(get(USER_STOCKS_URL))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void shouldReturnBadGatewayWhenExternalServiceIsNotReachableWhileGettingUserStocks() throws Exception {
        Mockito.when(userWalletDetailsServiceMock.getUserStocks()).thenThrow(new ResourceAccessException("Read timed out"));

        mockMvc.perform(get(USER_STOCKS_URL))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value(502))
            .andExpect(jsonPath("$.message").value("External service could not be reached! Please try again."));
    }

    @Test
    void shouldReturnConflictWhenUniqueConstraintIsViolatedDuringRegistration() throws Exception {
        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class)))
            .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

        mockMvc.perform(post(USER_REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(USER_REGISTER_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("Request conflicts with the current state of the resource! Please try again."));
    }

    @Test
    void shouldReturnNotFoundWhenStockIsNotFoundWhileGettingStockData() throws Exception {
        Mockito.when(finnhubStockDataProviderMock.getStockData("AAPL")).thenThrow(StockNotFoundException.createFromTicker("AAPL"));

        mockMvc.perform(get(STOCK_DATA_URL))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void shouldNormalizeTickerWhileGettingStockData() throws Exception {
        Mockito.when(finnhubStockDataProviderMock.getStockData("AAPL")).thenThrow(StockNotFoundException.createFromTicker("AAPL"));

        mockMvc.perform(get("/api/public/stock/v2/aapl"))
            .andExpect(status().isNotFound());

        Mockito.verify(finnhubStockDataProviderMock).getStockData("AAPL");
    }

    @Test
    void shouldNormalizeTickerFromTransactionRequestBody() throws Exception {
        mockMvc.perform(post(TRANSACTION_CREATE_URL).contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"type": "PURCHASE", "stockTicker": " aapl ", "price": 20.25, "quantity": 10}
                    """))
            .andExpect(status().isOk());

        final ArgumentCaptor<TransactionRequestDTO> captor = ArgumentCaptor.forClass(TransactionRequestDTO.class);
        Mockito.verify(transactionServiceMock).validateCreateAndProcessTransaction(captor.capture());
        assertEquals("AAPL", captor.getValue().stockTicker());
    }

    @Test
    void shouldReturnBadGatewayWhenFinnhubConnectionFailsWhileGettingStockData() throws Exception {
        Mockito.when(finnhubStockDataProviderMock.getStockData("AAPL")).thenThrow(FinnhubConnectionFailedException.create(429));

        mockMvc.perform(get(STOCK_DATA_URL))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value(502));
    }

}
