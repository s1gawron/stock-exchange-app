package pl.eizodev.app.controllers;

import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.eizodev.app.entities.StockIndex;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stockstats.StockFactory;

import java.util.Optional;

@Controller
class MainController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final StockService stockService;

    public MainController(UserRepository userRepository, UserService userService, StockService stockService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.stockService = stockService;
    }

    @GetMapping("/mainView")
    public String mainView(@CurrentSecurityContext(expression = "authentication.name") String username, Model model) {
        Optional<User> userOptional = userRepository.findByName(username);
        userOptional.ifPresent(user -> model.addAttribute("user", user));

        return "index";
    }

    @GetMapping("/stockListings/{index}")
    public String stockListings(@PathVariable StockIndex index, @CurrentSecurityContext(expression = "authentication.name") String username, Model model) {
        StockFactory stockFactory = new StockFactory();

        model.addAttribute("stocks", stockFactory.getAllStocksFromGivenIndex(index));
        model.addAttribute("name", username);

        return "stockStats";
    }

    @GetMapping("/myWallet")
    public String myWallet(@CurrentSecurityContext(expression = "authentication.name") String username, Model model) {
        stockService.updateStock(username);
        userService.updateUser(username);
        Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            model.addAttribute("userStock", userOptional.get().getUserStock());
        }

        return "myWallet";
    }
}