package com.s1gawron.stockexchange.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.jwt.exception.UntrustedTokenException;
import com.s1gawron.stockexchange.shared.ErrorResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JwtTokenVerifier extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain)
        throws ServletException, IOException {
        final String authorizationHeader = httpServletRequest.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        try {
            final Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes()))
                .build()
                .parseClaimsJws(jwtConfig.getJwtTokenFromAuthorizationHeader(authorizationHeader))
                .getBody();

            final String username = claims.getSubject();
            final List<Map<String, String>> authorities = (List<Map<String, String>>) claims.get("authorities");
            final Set<SimpleGrantedAuthority> simpleGrantedAuthoritySet = authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.get("authority")))
                .collect(Collectors.toSet());
            final Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, simpleGrantedAuthoritySet);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException e) {
            final ObjectMapper objectMapper = new ObjectMapper();

            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

            final ErrorResponse errorResponse = new ErrorResponse(Instant.now().toString(), HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Token cannot be trusted!", httpServletRequest.getRequestURI());

            objectMapper.writeValue(httpServletResponse.getWriter(), errorResponse);

            throw UntrustedTokenException.create();
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
