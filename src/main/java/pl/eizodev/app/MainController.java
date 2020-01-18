package pl.eizodev.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    StockWIG20 stockWIG20 = new StockWIG20();

    @GetMapping("/mainView")
    public String mainView(Model model) {
        model.addAttribute("stocks", stockWIG20.getAll());

        return "index";
    }
}
