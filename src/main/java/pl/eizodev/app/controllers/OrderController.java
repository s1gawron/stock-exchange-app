package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.offlineUser.OfflineStockTransaction;
import pl.eizodev.app.utilities.UserUtilities;
import pl.eizodev.app.validators.TransactionValidator;
import pl.eizodev.app.webScrape.StocksStats;

@Controller
public class OrderController {

    private User user;

    @Autowired
    UserService userService;

    @Autowired
    OfflineStockTransaction offlineStockTransaction;

    @GetMapping("/stockListings/{index}/{ticker}/{action}")
    public String orderForm(@PathVariable String index, @PathVariable String ticker, Model model) {

        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);
        StocksStats stocksStats = new StocksStats();

        model.addAttribute("user", user);
        model.addAttribute("stock", stocksStats.getByTicker(stocksStats.getAllStocksFromGivenIndex(index), ticker));
        model.addAttribute("transaction", new Transaction());

        return "orderForm";
    }

    @PostMapping("/process-order")
    public String processOrderForm(@ModelAttribute Transaction transaction, BindingResult result, Model model) {

        String returnPage = null;
        String username = UserUtilities.getLoggedUser();
        user = userService.findByName(username);
        StocksStats stocksStats = new StocksStats();
        String ticker = transaction.getStockTicker();
        String index = transaction.getStockIndex();

        new TransactionValidator(userService).validate(transaction, result);

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("stock", stocksStats.getByTicker(stocksStats.getAllStocksFromGivenIndex(index), ticker));

            returnPage = "orderForm";
        } else {
            offlineStockTransaction.performTransaction(transaction);
            returnPage = "redirect:/myWallet";
        }

        return returnPage;
    }
}
