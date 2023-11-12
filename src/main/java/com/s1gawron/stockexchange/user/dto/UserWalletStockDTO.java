package com.s1gawron.stockexchange.user.dto;

import java.math.BigDecimal;

public record UserWalletStockDTO(String ticker, BigDecimal averagePurchasePrice, int quantity) {

}
