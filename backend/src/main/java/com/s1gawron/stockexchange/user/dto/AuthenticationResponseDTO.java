package com.s1gawron.stockexchange.user.dto;

import java.time.Instant;

public record AuthenticationResponseDTO(String token, String username, Instant expirationDate) {

}
