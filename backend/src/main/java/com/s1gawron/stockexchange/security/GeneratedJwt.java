package com.s1gawron.stockexchange.security;

import java.time.Instant;

public record GeneratedJwt(String token,
                           String username,
                           Instant expirationDate) {

}
