package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;

public record TransactionRequestDTO(TransactionType type, String stockTicker, BigDecimal price, int quantity) {

    public BigDecimal quantityBD() {
        return BigDecimal.valueOf(quantity);
    }

}
