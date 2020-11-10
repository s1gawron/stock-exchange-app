package pl.eizodev.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.offlineUser.OfflineStockTransaction;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.utilities.UserUtilities;
import pl.eizodev.app.validators.TransactionValidator;
import pl.eizodev.app.webScrape.StocksStats;

@Controller
class OrderController {

    private final UserService userService;
    private final OfflineStockTransaction offlineStockTransaction;

    public OrderController(UserService userService, OfflineStockTransaction offlineStockTransaction) {
        this.userService = userService;
        this.offlineStockTransaction = offlineStockTransaction;
    }

    @GetMapping("/stockListings/{index}/{ticker}/{action}")
    public String orderForm(@PathVariable String index, @PathVariable String ticker, Model model) {

        String username = UserUtilities.getLoggedUser();
        User user = userService.findByName(username);
        StocksStats stocksStats = new StocksStats();

        model.addAttribute("user", user);
        model.addAttribute("stocks", stocksStats.getAllStocksFromGivenIndex(index));
        model.addAttribute("stock", stocksStats.getByTicker(stocksStats.getAllStocksFromGivenIndex(index), ticker));
        model.addAttribute("transaction", new Transaction());

        return "orderForm";
    }

    @PostMapping("/process-order")
    public String processOrderForm(@ModelAttribute Transaction transaction, BindingResult result, Model model) {

        String username = UserUtilities.getLoggedUser();
        User user = userService.findByName(username);
        StocksStats stocksStats = new StocksStats();
        String ticker = transaction.getStockTicker();
        String index = transaction.getStockIndex();

        new TransactionValidator(userService).validate(transaction, result);

        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("stock", stocksStats.getByTicker(stocksStats.getAllStocksFromGivenIndex(index), ticker));

            return "orderForm";
        } else {
            offlineStockTransaction.performTransaction(transaction);
            return "redirect:/myWallet";
        }
    }
}
