package pl.eizodev.app.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.dto.StockDTO;
import pl.eizodev.app.dto.UserDTO;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.StockIndex;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stockstats.StockFactory;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
class MainController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final StockService stockService;
    private final StockFactory stockFactory;

    @GetMapping("myWallet")
    public ResponseEntity<UserDTO> getUserWalletDetails(@CurrentSecurityContext(expression = "authentication.name") String username) {
        stockService.updateStock(username);
        userService.updateUser(username);
        Optional<User> userOptional = userRepository.findByName(username);

        return userOptional.map(user -> ResponseEntity.ok().body(UserDTO.of(user))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("stockListings/{index}")
    public ResponseEntity<List<StockDTO>> getStockList(@PathVariable StockIndex index) {
        return ResponseEntity.ok().body(StockDTO.listOf(stockFactory.getAllStocksFromGivenIndex(index)));
    }

    @GetMapping("stockListings/{index}/{ticker}")
    public ResponseEntity<StockDTO> getStockDetails(@PathVariable StockIndex index, @PathVariable String ticker) {
        Optional<Stock> optionalStock = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

        return optionalStock.map(stock -> ResponseEntity.ok().body(StockDTO.of(stock))).orElseGet(() -> ResponseEntity.notFound().build());
    }
}