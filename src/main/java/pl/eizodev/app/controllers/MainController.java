package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.soloUser.StockActions;
import pl.eizodev.app.entity.Transaction;
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

    @Autowired
    private StockActions stockActions;

    @GetMapping("/mainView")
    public String mainView(Model model) {
        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);
        userService.updateUser(user);

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
        userService.updateUser(user);

        if (!user.getUserStock().isEmpty()) {
            stockService.updateStock(user);
        }

        model.addAttribute("user", user);
        model.addAttribute("userStock", user.getUserStock());

        return "mywallet";
    }

    @GetMapping("/order")
    public String orderForm(@RequestParam("ticker") String ticker, Model model) {
        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);

        model.addAttribute("user", user);
        model.addAttribute("stock", stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker));

        return "orderform";
    }

    @PostMapping("/process-order")
    public String processOrderForm(
            @RequestParam(value = "ticker") String ticker,
            @RequestParam(value = "action") String action,
            @RequestParam(value = "quantity") int quantity,
            @ModelAttribute Transaction transaction,
            BindingResult result
    ) {

        String returnPage = null;

//        new TransactionValidator().hasEnoughMoney(transaction, result);
//        new TransactionValidator().hasEnoughStock(transaction, result);

//        if (result.hasErrors()) {
//            returnPage = "order";
//        } else {
            if (action.equals("buy")) {
                stockActions.stockPurchase(quantity, ticker, user.getUserId());
            } else if (action.equals("sell")) {
                stockActions.stockSell(quantity, ticker, user.getUserId());
            }
            returnPage = "redirect:/stock/myWallet";
//        }

        return returnPage;
    }
}