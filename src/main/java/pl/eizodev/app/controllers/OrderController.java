package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.soloUser.StockActions;
import pl.eizodev.app.stocks.StockWIG20;
import pl.eizodev.app.utilities.UserUtilities;
import pl.eizodev.app.validators.TransactionValidator;

@Controller
public class OrderController {

    @Autowired
    UserService userService;

    @Autowired
    StockActions stockActions;

    @GetMapping("/order/{action}/{ticker}")
    public String orderForm(@PathVariable String ticker, Model model) {
        User user;
        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);
        StockWIG20 stockWIG20 = new StockWIG20();

        model.addAttribute("user", user);
        model.addAttribute("stock", stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker));
        model.addAttribute("transaction", new Transaction());

        return "orderform";
    }

    @PostMapping("/process-order")
    public String processOrderForm(@ModelAttribute Transaction transaction, BindingResult result, Model model) {

        String returnPage = null;
        String username = UserUtilities.getLoggedUser();
        User user = userService.findByName(username);
        StockWIG20 stockWIG20 = new StockWIG20();
        String ticker = transaction.getStockTicker();

        if (transaction.getTransactionType().equals("buy")) {
            new TransactionValidator(userService).hasEnoughMoney(transaction, result);
        } else if (transaction.getTransactionType().equals("sell")) {
            new TransactionValidator(userService).hasEnoughStock(transaction, result);
        }

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("stock", stockWIG20.getByTicker(stockWIG20.getAllStocksWIG20(), ticker));

            returnPage = "orderform";

        } else {
        stockActions.performTransaction(transaction);
        returnPage = "redirect:/stock/myWallet";
        }

        return returnPage;
    }
}
