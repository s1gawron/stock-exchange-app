package pl.eizodev.app.controllers;

import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.offlineuser.OfflineStockTransaction;
import pl.eizodev.app.repositories.UserRepository;
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
    public String orderForm(@PathVariable String index, @PathVariable String ticker, @CurrentSecurityContext(expression = "authentication.name") String username, Model model) {
        Optional<User> userOptional = userRepository.findByName(username);
        StockFactory stockFactory = new StockFactory();
        Optional<Stock> stockOptional = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

        userOptional.ifPresent(user -> model.addAttribute("user", user));
        model.addAttribute("stocks", stockFactory.getAllStocksFromGivenIndex(index));
        stockOptional.ifPresent(stock -> model.addAttribute("stock", stock));
        model.addAttribute("transaction", new Transaction());

        return "orderForm";
    }

    @PostMapping("/process-order")
    public String processOrderForm(@ModelAttribute Transaction transaction, @CurrentSecurityContext(expression = "authentication.name") String username, BindingResult result, Model model) {
        Optional<User> userOptional = userRepository.findByName(username);
        StockFactory stockFactory = new StockFactory();
        String ticker = transaction.getStockTicker();
        String index = transaction.getStockIndex();
        Optional<Stock> stockOptional = stockFactory.getByTicker(stockFactory.getAllStocksFromGivenIndex(index), ticker);

        new TransactionValidator(userRepository).validate(transaction, result);

        if (result.hasErrors()) {
            userOptional.ifPresent(user -> model.addAttribute("user", user));
            stockOptional.ifPresent(stock -> model.addAttribute("stock", stock));

            return "orderForm";
        } else {
            offlineStockTransaction.performTransaction(transaction);
            return "redirect:/myWallet";
        }
    }
}
