package pl.eizodev.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    StockWIG20 stockWIG20 = new StockWIG20();

    @GetMapping("/mainView")
    public String mainView() {
        return "index";
    }

    @GetMapping("/statsWIG20")
    public String statsWIG20(Model model) {
        model.addAttribute("stocks", stockWIG20.getAll());

        return "statsWIG20";
    }

    @GetMapping("/myWallet")
    public String myWallet() {
        return "mywallet";
    }

    @GetMapping("/order")
    public String orderForm() {
        return "orderform";
    }
}
