package com.s1gawron.stockexchange.user.dto;

import java.math.BigDecimal;

public record UserRegisterDTO(String username, String email, String password, BigDecimal userWalletBalance) {

}
