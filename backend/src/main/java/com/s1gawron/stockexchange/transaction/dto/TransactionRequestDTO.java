package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.stock.StockTickerNormalizer;
import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;

public record TransactionRequestDTO(TransactionType type, String stockTicker, BigDecimal price, BigDecimal quantity, Long positionId) {

    public TransactionRequestDTO {
        stockTicker = StockTickerNormalizer.I.normalize(stockTicker);
    }

    public TransactionRequestDTO(final TransactionType type, final String stockTicker, final BigDecimal price, final BigDecimal quantity) {
        this(type, stockTicker, price, quantity, null);
    }

}
