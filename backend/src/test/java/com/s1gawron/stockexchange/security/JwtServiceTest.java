package com.s1gawron.stockexchange.security;

import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String JWT_SECRET_KEY = "11111111111111111111111111111111";

    private Clock clock;

    private UserDetailsService userDetailsServiceMock;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        userDetailsServiceMock = Mockito.mock(UserDetailsService.class);
        jwtService = new JwtService(JWT_SECRET_KEY, userDetailsServiceMock, clock);
    }

    @Test
    void shouldGenerateToken() {
        final User user = UserCreatorHelper.I.createUser();
        final GeneratedJwt result = jwtService.generateToken(Map.of(), user);
        final String[] splitResult = result.token().split("\\.");

        assertTrue(isStringNotEmpty(result.token()));
        assertEquals(3, splitResult.length);
        assertTrue(isStringNotEmpty(splitResult[0]));
        assertTrue(isStringNotEmpty(splitResult[1]));
        assertTrue(isStringNotEmpty(splitResult[2]));
        assertEquals(user.getUsername(), result.username());
        assertEquals(clock.instant().plus(Duration.ofHours(1)).truncatedTo(ChronoUnit.SECONDS), result.expirationDate());
    }

    @Test
    void shouldValidateToken() {
        final User user = UserCreatorHelper.I.createUser();
        final String token = jwtService.generateToken(Map.of(), user).token();

        Mockito.when(userDetailsServiceMock.loadUserByUsername(user.getUsername())).thenReturn(user);

        final boolean result = jwtService.validateToken(token).tokenValid();

        assertTrue(result);
    }

    @Test
    void shouldNotValidateTokenWhenUserDetailsDoNotMatch() {
        final User user = UserCreatorHelper.I.createUser();
        final User differentUser = UserCreatorHelper.I.createDifferentUser();
        final String token = jwtService.generateToken(Map.of(), user).token();

        Mockito.when(userDetailsServiceMock.loadUserByUsername(user.getUsername())).thenReturn(differentUser);

        final boolean result = jwtService.validateToken(token).tokenValid();

        assertFalse(result);
    }

    @Test
    void shouldNotValidateTokenWhenUserIsDisabled() {
        final User user = UserCreatorHelper.I.createUser();
        final User disabledUser = new User(false,user.getUsername(), user.getEmail(), user.getPassword(), UserRole.USER);
        final String token = jwtService.generateToken(Map.of(), user).token();

        Mockito.when(userDetailsServiceMock.loadUserByUsername(user.getUsername())).thenReturn(disabledUser);

        final boolean result = jwtService.validateToken(token).tokenValid();

        assertFalse(result);
    }

    @Test
    void shouldNotValidateTokenWhenTokenIsExpiredAccordingToClock() {
        final User user = UserCreatorHelper.I.createUser();
        final String token = jwtService.generateToken(Map.of(), user).token();
        final Clock twoHoursLaterClock = Clock.fixed(Instant.now().plus(Duration.ofHours(2)), ZoneId.systemDefault());
        final JwtService twoHoursLaterJwtService = new JwtService(JWT_SECRET_KEY, userDetailsServiceMock, twoHoursLaterClock);

        assertThrows(ExpiredJwtException.class, () -> twoHoursLaterJwtService.validateToken(token));
    }

    @Test
    void shouldNotValidateTokenWhenTokenIsExpired() {
        final User user = UserCreatorHelper.I.createUser();
        final String token = generateExpiredToken(user);

        assertThrows(ExpiredJwtException.class, () -> jwtService.validateToken(token));
    }

    @Test
    void shouldNotValidateTokenWhenTokenIsInvalid() {
        assertThrows(MalformedJwtException.class, () -> jwtService.validateToken("xyz"));
    }

    private String generateExpiredToken(final User user) {
        final Duration oneDayDuration = Duration.ofDays(1);
        final Clock oneDayExpiredClock = Clock.fixed(Instant.now().minus(oneDayDuration), ZoneId.systemDefault());
        final JwtService expiredJwtService = new JwtService(JWT_SECRET_KEY, userDetailsServiceMock, oneDayExpiredClock);

        return expiredJwtService.generateToken(Map.of(), user).token();
    }

    private boolean isStringNotEmpty(final String s) {
        final String trimmedString = s != null ? s.trim() : "";
        return !trimmedString.isEmpty();
    }

}