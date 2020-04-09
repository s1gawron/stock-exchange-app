package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
//import pl.eizodev.app.StockActions;
import pl.eizodev.app.StockWIG20;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.UserService;

@Controller
@RequestMapping("/stock")
public class MainController {
    private StockWIG20 stockWIG20 = new StockWIG20();
    private User user;

    @Autowired
    private UserService userService;

    @GetMapping("/mainView")
    public String mainView(Model model) {
//        userDao.updateUser(1L);
        user = userService.findById(1L).get();
        model.addAttribute("user", user);

        return "index";
    }

    @GetMapping("/statsWIG20")
    public String statsWIG20(Model model) {
        user = userService.findById(1L).get();
        model.addAttribute("stocks", stockWIG20.getAll());
        model.addAttribute("user", user);

        return "statsWIG20";
    }

    @GetMapping("/myWallet")
    public String myWallet(Model model) {
//        userDao.updateUser(1L);

//        if (!user.getUserStock().isEmpty()) {
//            stockDao.updateStock(1L);
//        }

        user = userService.findById(1L).get();

        model.addAttribute("user", user);
        model.addAttribute("userStock", user.getUserStock());

        return "mywallet";
    }

    @GetMapping("/order")
    public String orderForm(@RequestParam("ticker") String ticker, Model model) {
        user = userService.findById(1L).get();
        model.addAttribute("user", user);
        model.addAttribute("stock", stockWIG20.getByTicker(ticker));

        return "orderform";
    }

    @PostMapping("/process-order")
    public String processOrderForm(
            @RequestParam(value = "ticker") String ticker,
            @RequestParam(value = "action") String action,
            @RequestParam(value = "quantity") int quantity
    ) {
//        StockActions stockActions = new StockActions();
        user = userService.findById(1L).get();

//        if (action.equals("buy")) {
//            stockActions.stockPurchase(quantity, ticker, user.getUserId());
//        } else if (action.equals("sell")) {
//            stockActions.stockSell(quantity, ticker, user.getUserId());
//        }

        return "redirect:/stock/myWallet";
    }
}