package pl.eizodev.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.offlineuser.OfflineStockTransaction;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.utilities.UserUtilities;
import pl.eizodev.app.validators.TransactionValidator;
import pl.eizodev.app.stockstats.StockFactory;

import java.util.Optional;

@Controller
class OrderController {

    private final UserRepository userRepository;
    private final OfflineStockTransaction offlineStockTransaction;

    public OrderController(UserRepository userRepository, OfflineStockTransaction offlineStockTransaction) {
        this.userRepository = userRepository;
        this.offlineStockTransaction = offlineStockTransaction;
    }

    @GetMapping("/stockListings/{index}/{ticker}/{action}")
    public String orderForm(@PathVariable String index, @PathVariable String ticker, Model model) {

        String username = UserUtilities.getLoggedUser();
        Optional<User> userOptional = userRepository.findByName(username);
        StockFactory stockFactory = new StockFactory();

        userOptional.ifPresent(user -> model.addAttribute("user", user));
        model.addAttribute("stocks", stockFactory.getAllStocksFromGivenIndex(index));
        model.addAttribute("stock", stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker));
        model.addAttribute("transaction", new Transaction());

        return "orderForm";
    }

    @PostMapping("/process-order")
    public String processOrderForm(@ModelAttribute Transaction transaction, BindingResult result, Model model) {

        String username = UserUtilities.getLoggedUser();
        Optional<User> userOptional = userRepository.findByName(username);
        StockFactory stockFactory = new StockFactory();
        String ticker = transaction.getStockTicker();
        String index = transaction.getStockIndex();

        new TransactionValidator(userRepository).validate(transaction, result);

        if (result.hasErrors()) {
            userOptional.ifPresent(user -> model.addAttribute("user", user));
            model.addAttribute("stock", stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker));

            return "orderForm";
        } else {
            offlineStockTransaction.performTransaction(transaction);
            return "redirect:/myWallet";
        }
    }
}
