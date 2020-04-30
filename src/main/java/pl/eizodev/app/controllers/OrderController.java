package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.soloUser.StockActions;
import pl.eizodev.app.stocks.StockWIG20;
import pl.eizodev.app.utilities.UserUtilities;

@Controller
public class OrderController {

    @Autowired
    UserService userService;

    @Autowired
    StockActions stockActions;

    @GetMapping("/order")
    public String orderForm(@RequestParam("ticker") String ticker, Model model) {
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
    public String processOrderForm(@ModelAttribute Transaction transaction, BindingResult result) {

        String returnPage = null;

//        new TransactionValidator().hasEnoughMoney(transaction, result);
//        new TransactionValidator().hasEnoughStock(transaction, result);

//        if (result.hasErrors()) {
//            returnPage = "order";
//        } else {
        stockActions.performTransaction(transaction);
        returnPage = "redirect:/stock/myWallet";
//        }

        return returnPage;
    }
}
