package com.s1gawron.stockexchange.stock.controller;

import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.FinnhubStockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.StockSearchDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.wikitable.IndexSymbol;
import com.s1gawron.stockexchange.stock.dataprovider.wikitable.WikiTableStockDataProvider;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/public/stock/v2")
public class StockControllerV2 extends StockErrorHandlerController {

    private final FinnhubStockDataProvider finnhubStockDataProvider;

    private final WikiTableStockDataProvider wikiTableStockDataProvider;

    public StockControllerV2(final FinnhubStockDataProvider finnhubStockDataProvider, final WikiTableStockDataProvider wikiTableStockDataProvider) {
        this.finnhubStockDataProvider = finnhubStockDataProvider;
        this.wikiTableStockDataProvider = wikiTableStockDataProvider;
    }

    @GetMapping("search")
    public StockSearchDTO findStock(@RequestParam final String query) {
        return finnhubStockDataProvider.findStock(query.toLowerCase());
    }

    @GetMapping("{ticker}")
    public StockDataDTO getStockData(@PathVariable final String ticker) {
        return finnhubStockDataProvider.getStockData(ticker);
    }

    @GetMapping("index/{symbol}")
    public IndexCompaniesDTO getIndexCompanies(@PathVariable final IndexSymbol symbol) {
        return wikiTableStockDataProvider.getIndexCompanies(symbol);
    }
}
