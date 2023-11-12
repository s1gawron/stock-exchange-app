package com.s1gawron.stockexchange.security;

import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.user.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String JWT_SECRET_KEY = "11111111111111111111111111111111";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        jwtService = new JwtService(JWT_SECRET_KEY, clock);
    }

    @Test
    void shouldGenerateToken() {
        final String result = jwtService.generateToken(Map.of(), UserCreatorHelper.I.createUser());
        final String[] splitResult = result.split("\\.");

        assertTrue(isStringNotEmpty(result));
        assertEquals(3, splitResult.length);
        assertTrue(isStringNotEmpty(splitResult[0]));
        assertTrue(isStringNotEmpty(splitResult[1]));
        assertTrue(isStringNotEmpty(splitResult[2]));
    }

    @Test
    void shouldValidateToken() {
        final User user = UserCreatorHelper.I.createUser();
        final String token = jwtService.generateToken(Map.of(), user);

        final boolean result = jwtService.isTokenValid(token, user);

        assertTrue(result);
    }

    @Test
    void shouldNotValidateTokenWhenUserDetailsDoNotMatch() {
        final User user = UserCreatorHelper.I.createUser();
        final User differentUser = UserCreatorHelper.I.createDifferentUser();
        final String token = jwtService.generateToken(Map.of(), user);

        final boolean result = jwtService.isTokenValid(token, differentUser);

        assertFalse(result);
    }

    @Test
    void shouldNotValidateTokenWhenTokenIsExpired() {
        final User user = UserCreatorHelper.I.createUser();
        final String token = generateExpiredToken(user);

        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(token, user));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        final User user = UserCreatorHelper.I.createUser();
        final String token = jwtService.generateToken(Map.of(), user);

        final String result = jwtService.extractUsername(token);

        assertEquals(user.getUsername(), result);
    }

    private static String generateExpiredToken(final User user) {
        final Duration oneDayDuration = Duration.ofDays(1);
        final Clock oneDayExpiredClock = Clock.fixed(Instant.now().minus(oneDayDuration), ZoneId.systemDefault());
        final JwtService expiredJwtService = new JwtService(JWT_SECRET_KEY, oneDayExpiredClock);

        return expiredJwtService.generateToken(Map.of(), user);
    }

    private boolean isStringNotEmpty(final String s) {
        final String trimmedString = s != null ? s.trim() : "";
        return !trimmedString.isEmpty();
    }

}