package com.s1gawron.stockexchange.security;

import org.springframework.security.core.userdetails.UserDetails;

public record JwtValidationResult(boolean tokenValid,
                                  UserDetails userDetails) {

}
