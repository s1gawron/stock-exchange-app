package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.security.GeneratedJwt;
import com.s1gawron.stockexchange.security.JwtService;
import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.user.dto.AuthenticationResponseDTO;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAuthenticationServiceTest {

    private static final String TOKEN = "token";

    private static final Instant EXPIRATION_DATE = Instant.parse("2026-01-01T12:00:00Z");

    private AuthenticationManager authManager;

    private JwtService jwtService;

    private UserAuthenticationService userAuthenticationService;

    @BeforeEach
    void setUp() {
        authManager = Mockito.mock(AuthenticationManager.class);
        jwtService = Mockito.mock(JwtService.class);

        userAuthenticationService = new UserAuthenticationService(authManager, jwtService);
    }

    @Test
    void shouldReturnTokenUsernameAndExpirationDateFromGeneratedJwtWhenLoggingIn() {
        final User user = UserCreatorHelper.I.createUser(1L, "TestUser");
        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        Mockito.when(authManager.authenticate(Mockito.any())).thenReturn(authentication);
        Mockito.when(jwtService.generateToken(Map.of(), user)).thenReturn(new GeneratedJwt(TOKEN, user.getUsername(), EXPIRATION_DATE));

        final AuthenticationResponseDTO result = userAuthenticationService.loginUser(new UserLoginDTO("testuser", "Start00!"));

        assertEquals(TOKEN, result.token());
        assertEquals("TestUser", result.username());
        assertEquals(EXPIRATION_DATE, result.expirationDate());
    }

}
