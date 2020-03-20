package pl.eizodev.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.eizodev.app.dao.UserDao;
import pl.eizodev.app.entity.User;

@Controller
public class MainController {
    private StockWIG20 stockWIG20 = new StockWIG20();
    private UserDao userDao = new UserDao();
    private User user = userDao.getUser(1L);

    @GetMapping("/mainView")
    public String mainView(Model model) {
        model.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/statsWIG20")
    public String statsWIG20(Model model) {
        model.addAttribute("stocks", stockWIG20.getAll());
        model.addAttribute("user", user);

        return "statsWIG20";
    }

    @GetMapping("/myWallet")
    public String myWallet(Model model) {
        model.addAttribute("user", user);
        model.addAttribute("userStock", user.getUserStock());

        return "mywallet";
    }

    @GetMapping("/order")
    public String orderForm(Model model) {
        model.addAttribute("user", user);

        return "orderform";
    }
}
