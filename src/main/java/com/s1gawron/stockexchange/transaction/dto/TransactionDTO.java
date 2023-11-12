package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.TransactionType;

import java.math.BigDecimal;

public record TransactionDTO(TransactionType transactionType, String stockTicker, BigDecimal stockPrice, int stockQuantity) {

}
