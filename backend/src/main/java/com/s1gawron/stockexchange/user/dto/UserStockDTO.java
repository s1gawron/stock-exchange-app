package com.s1gawron.stockexchange.user.dto;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.user.model.UserStock;

import java.math.BigDecimal;
import java.util.List;

public record UserStockDTO(String ticker,
                           String name,
                           String stockCurrency,
                           BigDecimal price,
                           BigDecimal priceChange,
                           double percentagePriceChange,
                           BigDecimal quantity,
                           BigDecimal quantityBlocked,
                           BigDecimal averagePurchasePrice,
                           BigDecimal profitLoss,
                           List<UserStockPositionDTO> positions) {

    public static UserStockDTO create(final UserStock userStock, final StockDataDTO stockData) {
        final BigDecimal currentPrice = stockData.stockQuote().currentPrice();
        final List<UserStockPositionDTO> positions = userStock.getPositions().stream()
            .map(position -> UserStockPositionDTO.create(position, currentPrice))
            .toList();
        final BigDecimal allStockProfitLoss = MoneyRounding.I.money(positions.stream()
            .map(UserStockPositionDTO::profitLoss)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        return new UserStockDTO(userStock.getTicker(),
            stockData.companyFullName(),
            userStock.getCurrency(),
            currentPrice,
            stockData.stockQuote().priceChange(),
            stockData.stockQuote().percentagePriceChange(),
            userStock.getQuantityAvailable(),
            userStock.getQuantityBlocked(),
            userStock.getAveragePurchasePrice(),
            allStockProfitLoss,
            positions);
    }
}
