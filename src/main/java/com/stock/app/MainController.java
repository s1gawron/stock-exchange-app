package com.stock.app;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("eventName", "FIFA 2018");
        return "src/main/resources/templates/index.html";
    }
}
