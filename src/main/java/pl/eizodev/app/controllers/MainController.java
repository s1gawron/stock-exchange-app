package pl.eizodev.app.controllers;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.dtos.StockDTO;
import pl.eizodev.app.dtos.UserDTO;
import pl.eizodev.app.entities.StockIndex;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.services.exceptions.AccountNotFoundException;
import pl.eizodev.app.stockstats.StockFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("stock")
@AllArgsConstructor
class MainController extends AbstractErrorHandlerController {

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