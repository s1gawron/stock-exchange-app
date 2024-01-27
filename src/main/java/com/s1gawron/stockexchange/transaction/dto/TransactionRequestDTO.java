package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;

public record TransactionRequestDTO(TransactionType transactionType, String stockTicker, BigDecimal stockPrice, int stockQuantity) {

}
