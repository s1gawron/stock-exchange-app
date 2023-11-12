package com.s1gawron.stockexchange.user.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.jwt.JwtConfig;
import com.s1gawron.stockexchange.shared.ErrorResponse;
import com.s1gawron.stockexchange.shared.ObjectMapperCreator;
import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletStockDTO;
import com.s1gawron.stockexchange.user.exception.*;
import com.s1gawron.stockexchange.user.service.UserService;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebMvcTest(UserControllerV2.class)
@ActiveProfiles("test")
@WithMockUser
class UserControllerV2Test {

    private static final UserRegisterDTO USER_REGISTER_DTO = new UserRegisterDTO("testUser", "test@test.pl", "Start00!", new BigDecimal("10000.0"));

    private static final String USER_REGISTER_ENDPOINT = "/api/v2/user/register";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSource dataSourceMock;

    @MockBean
    private JwtConfig jwtConfigMock;

    @MockBean
    private UserService userServiceMock;

    @MockBean
    private UserWalletService userWalletServiceMock;

    private final ObjectMapper objectMapper = ObjectMapperCreator.I.getMapper();

    private String userRegisterJson;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        userRegisterJson = objectMapper.writeValueAsString(USER_REGISTER_DTO);
    }

    @Test
    void shouldRegisterUser() throws Exception {
        final UserWalletDTO userWalletDTO = new UserWalletDTO(new BigDecimal("0.00"), new BigDecimal("10000.0"), new BigDecimal("10000.0"),
            new BigDecimal("10000.0"), new BigDecimal("0"), List.of(), LocalDateTime.now());
        final UserDTO userDTO = new UserDTO("testUser", "test@test.pl", userWalletDTO);

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenReturn(userDTO);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();
        final String jsonResult = result.getResponse().getContentAsString();
        final UserDTO userDTOResult = objectMapper.readValue(jsonResult, UserDTO.class);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertNotNull(userDTOResult);
        assertEquals(userDTO.username(), userDTOResult.username());
        assertEquals(userDTO.email(), userDTOResult.email());
        assertEquals(userDTO.userWallet().balanceAvailable(), userDTOResult.userWallet().balanceAvailable());
        assertEquals(userDTO.userWallet().walletValue(), userDTOResult.userWallet().walletValue());
    }

    @Test
    void shouldReturnConflictResponseWhenUserEmailAlreadyExists() throws Exception {
        final UserEmailExistsException userEmailExistsException = UserEmailExistsException.create();

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenThrow(userEmailExistsException);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.CONFLICT, userEmailExistsException.getMessage(), USER_REGISTER_ENDPOINT,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnConflictResponseWhenUsernameAlreadyExists() throws Exception {
        final UserNameExistsException userNameExistsException = UserNameExistsException.create();

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenThrow(userNameExistsException);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.CONFLICT, userNameExistsException.getMessage(), USER_REGISTER_ENDPOINT,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnNotFoundResponseWhenUserIsNotFound() throws Exception {
        final UserNotFoundException userNotFoundException = UserNotFoundException.create("testUser");

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenThrow(userNotFoundException);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.NOT_FOUND, userNotFoundException.getMessage(), USER_REGISTER_ENDPOINT,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnBadRequeestResponseWhenRegisterPropertiesAreEmpty() throws Exception {
        final UserRegisterEmptyPropertiesException userRegisterEmptyPropertiesException = UserRegisterEmptyPropertiesException.createForPassword();

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenThrow(userRegisterEmptyPropertiesException);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.BAD_REQUEST, userRegisterEmptyPropertiesException.getMessage(), USER_REGISTER_ENDPOINT,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnBadRequeestResponseWhenEmailPatternDoesNotMatch() throws Exception {
        final UserEmailPatternViolationException userEmailPatternViolationException = UserEmailPatternViolationException.create("test-test.pl");

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenThrow(userEmailPatternViolationException);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.BAD_REQUEST, userEmailPatternViolationException.getMessage(), USER_REGISTER_ENDPOINT,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnBadRequeestResponseWhenPasswordIsTooWeak() throws Exception {
        final UserPasswordTooWeakException userPasswordTooWeakException = UserPasswordTooWeakException.create();

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenThrow(userPasswordTooWeakException);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.BAD_REQUEST, userPasswordTooWeakException.getMessage(), USER_REGISTER_ENDPOINT,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnBadRequeestResponseWhenWalletBalanceIsLessThanZero() throws Exception {
        final UserWalletBalanceLessThanZeroException userWalletBalanceLessThanZeroException = UserWalletBalanceLessThanZeroException.create();

        Mockito.when(userServiceMock.validateAndRegisterUser(Mockito.any(UserRegisterDTO.class))).thenThrow(userWalletBalanceLessThanZeroException);

        final RequestBuilder request = MockMvcRequestBuilders.post(USER_REGISTER_ENDPOINT).content(userRegisterJson).contentType(MediaType.APPLICATION_JSON);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.BAD_REQUEST, userWalletBalanceLessThanZeroException.getMessage(), USER_REGISTER_ENDPOINT,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    @Test
    void shouldGetUserWallet() throws Exception {
        final UserWalletDTO userWalletDTO = new UserWalletDTO(new BigDecimal("0.00"), new BigDecimal("10000.0"), new BigDecimal("10000.0"),
            new BigDecimal("10000.0"), new BigDecimal("0"),
            List.of(
                new UserWalletStockDTO("AAPL", BigDecimal.valueOf(39.25), 10),
                new UserWalletStockDTO("AMZN", BigDecimal.valueOf(40.05), 30)
            ), LocalDateTime.now());

        Mockito.when(userWalletServiceMock.updateAndGetUserWallet("user")).thenReturn(userWalletDTO);

        final RequestBuilder request = MockMvcRequestBuilders.get("/api/v2/user/wallet");
        final MvcResult result = mockMvc.perform(request).andReturn();
        final String jsonResult = result.getResponse().getContentAsString();
        final UserWalletDTO userWalletDTOResult = objectMapper.readValue(jsonResult, UserWalletDTO.class);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertNotNull(userWalletDTOResult);
        assertEquals(userWalletDTO.walletValue(), userWalletDTOResult.walletValue());
        assertEquals(userWalletDTO.userStock().size(), userWalletDTOResult.userStock().size());
    }

    @Test
    void shouldReturnNotFoundResponseWhenUserWalletIsNotFound() throws Exception {
        final UserWalletNotFoundException userWalletNotFoundException = UserWalletNotFoundException.create("user");
        Mockito.when(userWalletServiceMock.updateAndGetUserWallet("user")).thenThrow(userWalletNotFoundException);

        final String endpoint = "/api/v2/user/wallet";
        final RequestBuilder request = MockMvcRequestBuilders.get(endpoint);
        final MvcResult result = mockMvc.perform(request).andReturn();

        assertErrorResponse(HttpStatus.NOT_FOUND, userWalletNotFoundException.getMessage(), endpoint,
            toErrorResponse(result.getResponse().getContentAsString()));
    }

    void assertErrorResponse(final HttpStatus expectedStatus, final String expectedMessage, final String expectedUri,
        final ErrorResponse actualErrorResponse) {
        assertEquals(expectedStatus.value(), actualErrorResponse.code());
        assertEquals(expectedStatus.getReasonPhrase(), actualErrorResponse.error());
        assertEquals(expectedMessage, actualErrorResponse.message());
        assertEquals(expectedUri, actualErrorResponse.URI());
    }

    private ErrorResponse toErrorResponse(final String responseMessage) throws JsonProcessingException {
        return objectMapper.readValue(responseMessage, ErrorResponse.class);
    }

}