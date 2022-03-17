package pl.eizodev.app.stock.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.stock.dataprovider.StockDataProvider;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dto.StockDataDTO;

@RestController
@RequestMapping("api/v2/stock")
@AllArgsConstructor
public class StockControllerV2 {

    private final StockDataProvider stockDataProvider;

    @GetMapping("search/{query}")
    public FinnhubStockSearchResponseDTO findStock(@PathVariable final String query) {
        return stockDataProvider.findStock(query);
    }

    @GetMapping("{ticker}")
    public StockDataDTO getStockData(@PathVariable final String ticker) {
        return stockDataProvider.getStockData(ticker);
    }
}
