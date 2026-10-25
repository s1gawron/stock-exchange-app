package com.s1gawron.stockexchange.user.stock.favourite.dto;

import com.s1gawron.stockexchange.stock.StockTickerNormalizer;

public record AddFavouriteStockRequestDTO(String ticker) {

    public AddFavouriteStockRequestDTO {
        ticker = StockTickerNormalizer.I.normalize(ticker);
    }

}
