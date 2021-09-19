package pl.eizodev.app.stock.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.stock.StockFactory;
import pl.eizodev.app.stock.StockIndex;
import pl.eizodev.app.stock.dto.StockDTO;

import java.util.List;

@RestController
@RequestMapping("stock")
@AllArgsConstructor
public class StockController extends StockErrorHandlerController {

    private final StockFactory stockFactory;

    @GetMapping("{index}")
    public List<StockDTO> getStockList(@PathVariable final StockIndex index) {
        return StockDTO.listOf(stockFactory.getAllStocksFromGivenIndex(index));
    }

    @GetMapping("{index}/{ticker}")
    public StockDTO getStockDetails(@PathVariable final StockIndex index, @PathVariable final String ticker) {
        return StockDTO.of(stockFactory.getByTicker(index, ticker));
    }
}
