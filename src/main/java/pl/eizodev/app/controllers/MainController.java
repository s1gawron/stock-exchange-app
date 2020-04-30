package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.StockService;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.stocks.StockWIG20;
import pl.eizodev.app.utilities.UserUtilities;

@Controller
@RequestMapping("/stock")
public class MainController {
    private StockWIG20 stockWIG20 = new StockWIG20();
    private User user;

    @Autowired
    private UserService userService;

    @Autowired
    private StockService stockService;

    @GetMapping("/mainView")
    public String mainView(Model model) {
        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);
//            userService.updateUser(user);

        model.addAttribute("user", user);

        return "index";
    }

    @GetMapping("/statsWIG20")
    public String statsWIG20(Model model) {
        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);

        model.addAttribute("stocks", stockWIG20.getAllStocksWIG20());
        model.addAttribute("user", user);

        return "statsWIG20";
    }

    @GetMapping("/myWallet")
    public String myWallet(Model model) {

        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);
//        userService.updateUser(user);

        if (!user.getUserStock().isEmpty()) {
            stockService.updateStock(user);
        }

        model.addAttribute("user", user);
        model.addAttribute("userStock", user.getUserStock());

        return "mywallet";
    }
}