package com.s1gawron.stockexchange.stock.controller;

import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubStockSearchDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/public/stock/v2")
public class StockControllerV2 extends StockErrorHandlerController {

    private final FinnhubStockDataProvider finnhubStockDataProvider;

    public StockControllerV2(final FinnhubStockDataProvider finnhubStockDataProvider) {
        this.finnhubStockDataProvider = finnhubStockDataProvider;
    }

    @GetMapping("search/{query}")
    public FinnhubStockSearchDTO findStock(@PathVariable final String query) {
        return finnhubStockDataProvider.findStock(query);
    }

    @GetMapping("{ticker}")
    public StockDataDTO getStockData(@PathVariable final String ticker) {
        return finnhubStockDataProvider.getStockData(ticker);
    }
}
