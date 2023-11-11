package com.s1gawron.stockexchange.jwt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@NoArgsConstructor
public class JwtConfig {

    @Value("${application.jwt.secretKey}")
    private String secretKey;

    @Value("${application.jwt.tokenExpirationInDays}")
    private int tokenExpirationInDays;

    public String getJwtTokenFromAuthorizationHeader(final String tokenFromHeader) {
        return tokenFromHeader.replace("Bearer ", "");
    }
}
