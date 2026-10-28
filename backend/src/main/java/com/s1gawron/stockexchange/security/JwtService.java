package com.s1gawron.stockexchange.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey secretKey;

    private final UserDetailsService userDetailsService;

    private final Clock clock;

    public JwtService(@Value("${application.jwt.secretKey}") final String secretKey, final UserDetailsService userDetailsService, final Clock clock) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.userDetailsService = userDetailsService;
        this.clock = clock;
    }

    public GeneratedJwt generateToken(final Map<String, Object> extraClaims, final UserDetails userDetails) {
        final Duration oneHourDuration = Duration.ofHours(1);
        // JWT stores the expiration in whole seconds, so the returned date is truncated to match the token
        final Instant expireAfterOneHour = clock.instant().plus(oneHourDuration).truncatedTo(ChronoUnit.SECONDS);

        final String token = Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(Date.from(clock.instant()))
            .expiration(Date.from(expireAfterOneHour))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact();

        return new GeneratedJwt(token, userDetails.getUsername(), expireAfterOneHour);
    }

    public JwtValidationResult validateToken(final String token) {
        final String username = extractClaims(token).getSubject();
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final boolean tokenValid = username.equals(userDetails.getUsername()) && userDetails.isEnabled();

        return new JwtValidationResult(tokenValid, userDetails);
    }

    private Claims extractClaims(final String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .clock(() -> Date.from(clock.instant()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
