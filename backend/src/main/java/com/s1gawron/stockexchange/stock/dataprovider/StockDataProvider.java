package com.s1gawron.stockexchange.stock.dataprovider;

import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.StockSearchDTO;

public interface StockDataProvider {

    StockSearchDTO findStock(String query);

    StockDataDTO getStockData(String ticker);

}
