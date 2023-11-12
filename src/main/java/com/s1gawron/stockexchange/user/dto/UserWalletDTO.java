package com.s1gawron.stockexchange.user.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record UserWalletDTO(BigDecimal stockValue, BigDecimal balanceAvailable, BigDecimal walletValue, BigDecimal previousWalletValue,
                            BigDecimal walletPercentageChange, List<UserWalletStockDTO> userStock, LocalDateTime lastUpdateDate) {

}
