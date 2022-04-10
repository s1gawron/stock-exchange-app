package pl.eizodev.app.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import pl.eizodev.app.user.dto.UserLoginDTO;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.dto.UserWalletDTO;
import pl.eizodev.app.user.service.UserService;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userService.deleteUser("testUser");
        final UserRegisterDTO userRegisterDTO = new UserRegisterDTO("testUser", "test@test.pl", "Start00!", BigDecimal.valueOf(10));
        userService.validateAndRegisterUser(userRegisterDTO);
    }

    @Test
    @SneakyThrows
    void shouldLoginAndReturnValidTokenInHeader() {
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
    @SneakyThrows
    void shouldLoginAndGetSecuredResource() {
        final UserLoginDTO userLoginDTO = new UserLoginDTO("testUser", "Start00!");
        final String userLoginJson = objectMapper.writeValueAsString(userLoginDTO);
        final RequestBuilder loginRequest = MockMvcRequestBuilders.post("/api/v2/user/login").content(userLoginJson);
        final MvcResult loginResult = mockMvc.perform(loginRequest).andReturn();
        final String token = loginResult.getResponse().getHeader("Authorization");

        final RequestBuilder resourceRequest = MockMvcRequestBuilders.get("/api/v2/user/wallet").header("Authorization", token);
        final MvcResult resourceResult = mockMvc.perform(resourceRequest).andReturn();
        final String jsonResult = resourceResult.getResponse().getContentAsString();
        final UserWalletDTO userWalletDTOResult = objectMapper.readValue(jsonResult, UserWalletDTO.class);

        assertEquals(new BigDecimal("0"), userWalletDTOResult.getStockValue());
        assertEquals(new BigDecimal("10.00"), userWalletDTOResult.getBalanceAvailable());
        assertEquals(new BigDecimal("10.00"), userWalletDTOResult.getWalletValue());
        assertEquals(new BigDecimal("10.00"), userWalletDTOResult.getPreviousWalletValue());
        assertEquals(new BigDecimal("0.00"), userWalletDTOResult.getWalletPercentageChange());
        assertEquals(0, userWalletDTOResult.getUserStock().size());
    }

    @Test
    @SneakyThrows
    void shouldReturnUnauthorizedStatus() {
        final UserLoginDTO userLoginDTO = new UserLoginDTO("testUser", "wrongPassword");
        final String userLoginJson = objectMapper.writeValueAsString(userLoginDTO);
        final RequestBuilder request = MockMvcRequestBuilders.post("/api/v2/user/login").content(userLoginJson);

        final MvcResult result = mockMvc.perform(request).andReturn();
        final String token = result.getResponse().getHeader("Authorization");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
        assertNull(token);
    }

}
