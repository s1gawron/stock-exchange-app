package com.s1gawron.stockexchange.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.shared.ObjectMapperCreator;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerV2LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = ObjectMapperCreator.I.getMapper();

    @BeforeEach
    void setUp() {
        userService.deleteUser("testUser");
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("testUser", "test@test.pl", "Start00!", BigDecimal.valueOf(10));
        userService.validateAndRegisterUser(userRegisterDTO);
    }

    @Test
    void shouldLoginAndReturnValidTokenInHeader() throws Exception {
        final UserLoginDTO userLoginDTO = new UserLoginDTO("testUser", "Start00!");
        final String userLoginJson = objectMapper.writeValueAsString(userLoginDTO);
        final RequestBuilder request = MockMvcRequestBuilders.post("/api/v2/user/login").content(userLoginJson);

        final MvcResult result = mockMvc.perform(request).andReturn();
        final String token = result.getResponse().getHeader("Authorization");

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer"));
    }

    @Test
    void shouldLoginAndGetSecuredResource() throws Exception {
        final UserLoginDTO userLoginDTO = new UserLoginDTO("testUser", "Start00!");
        final String userLoginJson = objectMapper.writeValueAsString(userLoginDTO);
        final RequestBuilder loginRequest = MockMvcRequestBuilders.post("/api/v2/user/login").content(userLoginJson);
        final MvcResult loginResult = mockMvc.perform(loginRequest).andReturn();
        final String token = loginResult.getResponse().getHeader("Authorization");

        final RequestBuilder resourceRequest = MockMvcRequestBuilders.get("/api/v2/user/wallet").header("Authorization", token);
        final MvcResult resourceResult = mockMvc.perform(resourceRequest).andReturn();
        final String jsonResult = resourceResult.getResponse().getContentAsString();
        final UserWalletDTO userWalletDTOResult = objectMapper.readValue(jsonResult, UserWalletDTO.class);

        assertEquals(new BigDecimal("0"), userWalletDTOResult.stockValue());
        assertEquals(new BigDecimal("10.00"), userWalletDTOResult.balanceAvailable());
        assertEquals(new BigDecimal("10.00"), userWalletDTOResult.walletValue());
        assertEquals(new BigDecimal("10.00"), userWalletDTOResult.previousWalletValue());
        assertEquals(new BigDecimal("0.00"), userWalletDTOResult.walletPercentageChange());
        assertEquals(0, userWalletDTOResult.userStock().size());
    }

    @Test
    void shouldReturnUnauthorizedStatus() throws Exception {
        final UserLoginDTO userLoginDTO = new UserLoginDTO("testUser", "wrongPassword");
        final String userLoginJson = objectMapper.writeValueAsString(userLoginDTO);
        final RequestBuilder request = MockMvcRequestBuilders.post("/api/v2/user/login").content(userLoginJson);

        final MvcResult result = mockMvc.perform(request).andReturn();
        final String token = result.getResponse().getHeader("Authorization");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
        assertNull(token);
    }

}
