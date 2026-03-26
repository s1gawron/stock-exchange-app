package com.s1gawron.stockexchange.user.stock.favourite.controller;

import com.s1gawron.stockexchange.user.stock.favourite.dto.AddFavouriteStockRequestDTO;
import com.s1gawron.stockexchange.user.stock.favourite.dto.UserFavouriteStockDTO;
import com.s1gawron.stockexchange.user.stock.favourite.service.UserFavouriteStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/user/v1/stock/favourites")
public class UserFavouriteStockController extends UserFavouriteStockErrorHandlerController {

    private final UserFavouriteStockService userFavouriteStockService;

    public UserFavouriteStockController(final UserFavouriteStockService userFavouriteStockService) {
        this.userFavouriteStockService = userFavouriteStockService;
    }

    @PostMapping("add")
    public UserFavouriteStockDTO addFavouriteStock(@RequestBody final AddFavouriteStockRequestDTO requestDTO) {
        return userFavouriteStockService.addFavouriteStock(requestDTO);
    }

    @DeleteMapping("remove/{ticker}")
    public ResponseEntity<Void> removeFavouriteStock(@PathVariable final String ticker) {
        userFavouriteStockService.removeFavouriteStock(ticker);
        return ResponseEntity.ok().build();
    }

    @GetMapping("list")
    public List<UserFavouriteStockDTO> getUserFavouriteStocks() {
        return userFavouriteStockService.getUserFavouriteStocks();
    }
}
