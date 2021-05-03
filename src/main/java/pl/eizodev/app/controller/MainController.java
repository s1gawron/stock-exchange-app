package pl.eizodev.app.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.dto.StockDTO;
import pl.eizodev.app.dto.UserDTO;
import pl.eizodev.app.entity.StockIndex;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.repository.UserRepository;
import pl.eizodev.app.service.StockService;
import pl.eizodev.app.service.UserService;
import pl.eizodev.app.service.exception.AccountNotFoundException;
import pl.eizodev.app.stockstats.StockFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("stock")
@AllArgsConstructor
public class MainController extends AbstractErrorHandlerController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final StockService stockService;
    private final StockFactory stockFactory;

    @GetMapping("myWallet")
    public UserDTO getUserWalletDetails(@CurrentSecurityContext(expression = "authentication.name") final String username) {
        stockService.updateStock(username);
        userService.updateUser(username);
        final Optional<User> userOptional = userRepository.findByName(username);

        return userOptional.map(UserDTO::of).orElseThrow(() -> {
            throw AccountNotFoundException.create(username);
        });
    }

    @GetMapping("stockListings/{index}")
    public List<StockDTO> getStockList(@PathVariable final StockIndex index) {
        return StockDTO.listOf(stockFactory.getAllStocksFromGivenIndex(index));
    }

    @GetMapping("stockListings/{index}/{ticker}")
    public StockDTO getStockDetails(@PathVariable final StockIndex index, @PathVariable final String ticker) {
        return StockDTO.of(stockFactory.getByTicker(index, ticker));
    }
}