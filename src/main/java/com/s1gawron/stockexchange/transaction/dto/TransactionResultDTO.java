package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class TransactionResultDTO {
    private final Long userId;
    private final String username;
    private final TransactionType transactionType;
    private final String stockName;
    private final int stockQuantity;
    private final BigDecimal transactionCost;
    private final BigDecimal balanceAfterTransaction;
}
