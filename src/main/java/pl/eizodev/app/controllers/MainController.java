package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.utilities.UserUtilities;
import pl.eizodev.app.webScrape.StocksStats;

@Controller
@RequestMapping("/stock")
public class MainController {
    private StocksStats stocksStats = new StocksStats();
    private User user;

    @Autowired
    private UserService userService;

    @Autowired
    private StockService stockService;

    @GetMapping("/mainView")
    public String mainView(Model model) {
        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);

        model.addAttribute("user", user);

        return "index";
    }

    @GetMapping("/statsWIG20")
    public String statsWIG20(Model model) {
        String username = UserUtilities.getLoggedUser();

        model.addAttribute("stocks", stocksStats.getAllStocksWIG20());
        model.addAttribute("name", username);

        return "stockStats";
    }

    @GetMapping("/myWallet")
    public String myWallet(Model model) {
        String username = UserUtilities.getLoggedUser();

        stockService.updateStock(username);
        userService.updateUser(username);
        user = userService.findByName(username);

        model.addAttribute("user", user);
        model.addAttribute("userStock", user.getUserStock());

        return "myWallet";
    }
}