package com.s1gawron.stockexchange.stock.controller;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/public/stock/v2")
public class StockControllerV2 extends StockErrorHandlerController {

    private final StockDataProvider stockDataProvider;

    public StockControllerV2(final StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    @GetMapping("search/{query}")
    public FinnhubStockSearchResponseDTO findStock(@PathVariable final String query) {
        return stockDataProvider.findStock(query);
    }

    @GetMapping("{ticker}")
    public StockDataDTO getStockData(@PathVariable final String ticker) {
        return stockDataProvider.getStockData(ticker);
    }
}
