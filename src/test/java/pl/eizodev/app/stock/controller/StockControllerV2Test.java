package pl.eizodev.app.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.eizodev.app.jwt.JwtConfig;
import pl.eizodev.app.shared.ErrorResponse;
import pl.eizodev.app.stock.dataprovider.StockDataProvider;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dto.StockDataDTO;
import pl.eizodev.app.stock.exception.FinnhubConnectionFailedException;
import pl.eizodev.app.stock.exception.StockNotFoundException;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(StockControllerV2.class)
@ActiveProfiles("test")
class StockControllerV2Test {

    private static final String THIS_STOCK_DOES_NOT_EXIST = "thisstockdoesnotexist";

    private static final String STOCK_TICKER = "AAPl";

    private static final String STOCK_SEARCH_ENDPOINT_TEMPLATE = "/api/v2/stock/search/";

    private static final String STOCK_DATA_ENDPOINT_TEMPLATE = "/api/v2/stock/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private JwtConfig jwtConfig;

    @MockBean
    private StockDataProvider stockDataProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldReturnOkResponseForFindStockMethod() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/symbol-lookup-response.json"));
        final FinnhubStockSearchResponseDTO stockSearchResponse = objectMapper.readValue(jsonResponse, FinnhubStockSearchResponseDTO.class);

        Mockito.when(stockDataProvider.findStock("AAPL")).thenReturn(stockSearchResponse);

        final RequestBuilder request = MockMvcRequestBuilders.get("/api/v2/stock/search/AAPL");
        MvcResult result = mockMvc.perform(request).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @SneakyThrows
    void shouldReturnOkResponseForGetStockDataMethod() {
        final String jsonResponse = Files.readString(Path.of("src/test/resources/stock-data-response.json"));
        final StockDataDTO stockDataResponse = objectMapper.readValue(jsonResponse, StockDataDTO.class);

        Mockito.when(stockDataProvider.getStockData("AAPL")).thenReturn(stockDataResponse);

        final RequestBuilder request = MockMvcRequestBuilders.get("/api/v2/stock/AAPL");
        MvcResult result = mockMvc.perform(request).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @SneakyThrows
    void shouldReturnBadGatewayResponseForFindStockMethod() {
        final String endpoint = STOCK_SEARCH_ENDPOINT_TEMPLATE + STOCK_TICKER;
        final RequestBuilder request = MockMvcRequestBuilders.get(endpoint);
        final FinnhubConnectionFailedException connectionFailedException = FinnhubConnectionFailedException.create();

        Mockito.when(stockDataProvider.findStock(STOCK_TICKER)).thenThrow(connectionFailedException);
        MvcResult result = mockMvc.perform(request).andReturn();

        final String responseBody = result.getResponse().getContentAsString();

        assertErrorResponse(HttpStatus.BAD_GATEWAY, connectionFailedException.getMessage(), endpoint, result.getResponse().getStatus(),
            toErrorResponse(responseBody));
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundResponseForFindStockMethod() {
        final StockNotFoundException stockNotFoundException = StockNotFoundException.createFromQuery(THIS_STOCK_DOES_NOT_EXIST);
        final String endpoint = STOCK_SEARCH_ENDPOINT_TEMPLATE + THIS_STOCK_DOES_NOT_EXIST;
        final RequestBuilder request = MockMvcRequestBuilders.get(endpoint);

        Mockito.when(stockDataProvider.findStock(THIS_STOCK_DOES_NOT_EXIST)).thenThrow(stockNotFoundException);
        MvcResult result = mockMvc.perform(request).andReturn();

        final String responseBody = result.getResponse().getContentAsString();

        assertErrorResponse(HttpStatus.NOT_FOUND, stockNotFoundException.getMessage(), endpoint, result.getResponse().getStatus(),
            toErrorResponse(responseBody));
    }

    @Test
    @SneakyThrows
    void shouldReturnBadGatewayResponseForGetStockDataMethod() {
        final String endpoint = STOCK_DATA_ENDPOINT_TEMPLATE + STOCK_TICKER;
        final RequestBuilder request = MockMvcRequestBuilders.get(endpoint);
        final FinnhubConnectionFailedException connectionFailedException = FinnhubConnectionFailedException.create();

        Mockito.when(stockDataProvider.getStockData(STOCK_TICKER)).thenThrow(connectionFailedException);
        MvcResult result = mockMvc.perform(request).andReturn();

        final String responseBody = result.getResponse().getContentAsString();

        assertErrorResponse(HttpStatus.BAD_GATEWAY, connectionFailedException.getMessage(), endpoint, result.getResponse().getStatus(),
            toErrorResponse(responseBody));
    }

    @Test
    @SneakyThrows
    void shouldReturnNotFoundResponseForGetStockDataMethod() {
        final StockNotFoundException stockNotFoundException = StockNotFoundException.createFromTicker(THIS_STOCK_DOES_NOT_EXIST);
        final String endpoint = STOCK_DATA_ENDPOINT_TEMPLATE + THIS_STOCK_DOES_NOT_EXIST;
        final RequestBuilder request = MockMvcRequestBuilders.get(endpoint);

        Mockito.when(stockDataProvider.getStockData(THIS_STOCK_DOES_NOT_EXIST)).thenThrow(stockNotFoundException);
        MvcResult result = mockMvc.perform(request).andReturn();

        final String responseBody = result.getResponse().getContentAsString();

        assertErrorResponse(HttpStatus.NOT_FOUND, stockNotFoundException.getMessage(), endpoint, result.getResponse().getStatus(),
            toErrorResponse(responseBody));
    }

    private void assertErrorResponse(final HttpStatus expectedStatus, final String expectedMessage, final String expectedUri, final int actualStatusCode,
        final ErrorResponse actualErrorResponse) {
        Assertions.assertAll(
            () -> assertEquals(expectedStatus.value(), actualStatusCode),
            () -> assertEquals(expectedStatus.value(), actualErrorResponse.getCode()),
            () -> assertEquals(expectedStatus.getReasonPhrase(), actualErrorResponse.getError()),
            () -> assertEquals(expectedMessage, actualErrorResponse.getMessage()),
            () -> assertEquals(expectedUri, actualErrorResponse.getURI())
        );
    }

    @SneakyThrows
    private ErrorResponse toErrorResponse(final String responseMessage) {
        return objectMapper.readValue(responseMessage, ErrorResponse.class);
    }

}