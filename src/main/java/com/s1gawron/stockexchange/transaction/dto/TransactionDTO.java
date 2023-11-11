package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class TransactionDTO {
    private final TransactionType transactionType;
    private final String stockTicker;
    private final BigDecimal stockPrice;
    private final int stockQuantity;
}
