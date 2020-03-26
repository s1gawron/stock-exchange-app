package pl.eizodev.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.eizodev.app.dao.UserDao;
import pl.eizodev.app.entity.User;

@Controller
@RequestMapping("/stock")
public class MainController {
    private StockWIG20 stockWIG20 = new StockWIG20();
    private UserDao userDao = new UserDao();
    private User user;

    @GetMapping("/mainView")
    public String mainView(Model model) {
        user = userDao.getUser(1L);
        model.addAttribute("user", user);

        return "index";
    }

    @GetMapping("/statsWIG20")
    public String statsWIG20(Model model) {
        User user = userDao.getUser(1L);
        model.addAttribute("stocks", stockWIG20.getAll());
        model.addAttribute("user", user);

        return "statsWIG20";
    }

    @GetMapping("/myWallet")
    public String myWallet(Model model) {
        user = userDao.getUser(1L);
        model.addAttribute("user", user);
        model.addAttribute("userStock", user.getUserStock());

        return "mywallet";
    }

    @GetMapping("/order")
    public String orderForm(Model model) {
        user = userDao.getUser(1L);
        model.addAttribute("user", user);

        return "orderform";
    }

    @PostMapping("/order")
    public String processOrderForm(
            @RequestParam(value = "ticker") String ticker,
            @RequestParam(value = "action") String action,
            @RequestParam(value = "quantity") int quantity
    ) {
        StockActions stockActions = new StockActions();
        user = userDao.getUser(1L);

        if (action.equals("buy")) {
            stockActions.stockPurchase(quantity, ticker, user.getUserId());
        } else if (action.equals("sell")) {
            stockActions.stockSell(quantity, ticker, user.getUserId());
        }

        return "redirect:/stock/myWallet";
    }
}
