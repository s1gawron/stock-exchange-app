package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;

public record TransactionResultDTO(Long userId, String username, TransactionType transactionType, String stockName, int stockQuantity,
                                   BigDecimal transactionCost, BigDecimal balanceAfterTransaction) {

}
