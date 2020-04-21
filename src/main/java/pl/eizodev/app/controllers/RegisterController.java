package pl.eizodev.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.validators.RegisterValidator;


@Controller
@RequestMapping("/user")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    MessageSource messageSource;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("newUser", new User());

        return "registerForm";
    }

    @PostMapping("/add-user")
    public String processRegisterForm(@ModelAttribute("newUser") User user, BindingResult result) {
        String returnPage = null;

        User userNameExist = userService.findByName(user.getName());
        new RegisterValidator().userNameExist(userNameExist, result);

        User userEmailExist = userService.findByEmail(user.getEmail());
        new RegisterValidator().userEmailExist(userEmailExist, result);

        new RegisterValidator().invalidPasswordPattern(user, result);

        if (result.hasErrors()) {
            returnPage = "registerForm";
        } else {
            userService.saveUser(user);
            returnPage = "redirect:/login";
        }

        return returnPage;
    }
}