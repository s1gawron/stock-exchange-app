package pl.eizodev.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.utilities.UserUtilities;
import pl.eizodev.app.stockstats.StockFactory;

import java.util.Optional;

@Controller
class MainController {

    private final UserService userService;
    private final StockService stockService;

    public MainController(UserService userService, StockService stockService) {
        this.userService = userService;
        this.stockService = stockService;
    }

    @GetMapping("/mainView")
    public String mainView(Model model) {
        String username = UserUtilities.getLoggedUser();
        Optional<User> userOptional = userService.findByName(username);
        userOptional.ifPresent(user -> model.addAttribute("user", user));

        return "index";
    }

    @GetMapping("/stockListings/{index}")
    public String stockListings(@PathVariable String index, Model model) {
        String username = UserUtilities.getLoggedUser();
        StockFactory stockFactory = new StockFactory();

        model.addAttribute("stocks", stockFactory.getAllStocksFromGivenIndex(index));
        model.addAttribute("name", username);

        return "stockStats";
    }

    @GetMapping("/myWallet")
    public String myWallet(Model model) {
        String username = UserUtilities.getLoggedUser();

        stockService.updateStock(username);
        userService.updateUser(username);
        Optional<User> userOptional = userService.findByName(username);

        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            model.addAttribute("userStock", userOptional.get().getUserStock());
        }

        return "myWallet";
    }
}