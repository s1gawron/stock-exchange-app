package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.stock.StockTickerNormalizer;
import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;

public record TransactionRequestDTO(TransactionType type, String stockTicker, BigDecimal price, int quantity) {

    public TransactionRequestDTO {
        stockTicker = StockTickerNormalizer.I.normalize(stockTicker);
    }

    public BigDecimal quantityBD() {
        return BigDecimal.valueOf(quantity);
    }

}
