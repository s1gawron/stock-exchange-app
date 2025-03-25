package com.s1gawron.stockexchange.stock.dataprovider;

import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.StockSearchDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.StockSearchDetailsDTO;

import java.util.ArrayList;
import java.util.List;

public class InMemoryStockDataProvider implements StockDataProvider {

    private final List<StockSearchDTO> stocksSearch = new ArrayList<>();

    private final List<StockDataDTO> stocksData = new ArrayList<>();

    @Override
    public StockSearchDTO findStock(final String query) {
        final List<StockSearchDetailsDTO> searchDetails = stocksSearch.stream()
            .flatMap(search -> search.result().stream())
            .filter(search -> search.symbol().equals(query))
            .toList();

        return new StockSearchDTO(searchDetails.size(), searchDetails);
    }

    @Override
    public StockDataDTO getStockData(final String ticker) {
        return stocksData.stream()
            .filter(stock -> stock.ticker().equals(ticker))
            .findFirst()
            .orElseThrow(() -> StockNotFoundException.createFromTicker(ticker));
    }

    public void addStockSearch(final StockSearchDTO stockSearchDTO) {
        stocksSearch.add(stockSearchDTO);
    }

    public void addStockData(final StockDataDTO stockDataDTO) {
        stocksData.add(stockDataDTO);
    }
}
