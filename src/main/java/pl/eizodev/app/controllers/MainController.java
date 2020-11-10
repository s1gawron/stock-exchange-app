package pl.eizodev.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.utilities.UserUtilities;
import pl.eizodev.app.stockstats.StockFactory;

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
        User user = userService.findByName(username);

        model.addAttribute("user", user);

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
        User user = userService.findByName(username);

        model.addAttribute("user", user);
        model.addAttribute("userStock", user.getUserStock());

        return "myWallet";
    }
}