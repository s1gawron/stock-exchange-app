package com.s1gawron.stockexchange.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final String secretKey;

    private final UserDetailsService userDetailsService;

    private final Clock clock;

    public JwtService(@Value("${application.jwt.secretKey}") final String secretKey, final UserDetailsService userDetailsService, final Clock clock) {
        this.secretKey = secretKey;
        this.userDetailsService = userDetailsService;
        this.clock = clock;
    }

    public String generateToken(final Map<String, Object> extraClaims, final UserDetails userDetails) {
        final Duration oneHourDuration = Duration.ofHours(1);
        final Instant expireAfterOneHour = clock.instant().plus(oneHourDuration);

        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(Date.from(clock.instant()))
            .setExpiration(Date.from(expireAfterOneHour))
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    public JwtValidationResult validateToken(final String token) {
        final String username = extractUsername(token);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final boolean tokenValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

        return new JwtValidationResult(tokenValid, userDetails);
    }

    private String extractUsername(final String token) {
        return extractClaims(token).getSubject();
    }

    private boolean isTokenExpired(final String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(final String token) {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
