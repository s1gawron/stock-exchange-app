package com.s1gawron.stockexchange.user.dto;

import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.model.UserStock;

import java.math.BigDecimal;

public record UserStockDTO(String ticker,
                           String name,
                           BigDecimal price,
                           BigDecimal priceChange,
                           double percentagePriceChange,
                           int quantity,
                           int quantityBlocked,
                           BigDecimal averagePurchasePrice,
                           BigDecimal profitLoss) {

    public static UserStockDTO create(final UserStock userStock, final StockDataDTO stockData) {
        final BigDecimal singleStockProfitLoss = stockData.stockQuote().currentPrice().subtract(userStock.getAveragePurchasePrice());
        final BigDecimal allStockProfitLoss = singleStockProfitLoss.multiply(BigDecimal.valueOf(userStock.getQuantityAvailable()));

        return new UserStockDTO(userStock.getTicker(),
            stockData.companyFullName(),
            stockData.stockQuote().currentPrice(),
            stockData.stockQuote().priceChange(),
            stockData.stockQuote().percentagePriceChange(),
            userStock.getQuantityAvailable(),
            userStock.getQuantityBlocked(),
            userStock.getAveragePurchasePrice(),
            allStockProfitLoss);
    }
}
