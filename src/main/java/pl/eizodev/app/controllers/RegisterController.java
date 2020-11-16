package pl.eizodev.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.validators.RegisterValidator;

import java.util.Optional;


@Controller
@RequestMapping("/user")
class RegisterController {

    private final UserService userService;

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

        new RegisterValidator().validate(user, result);

        Optional<User> userNameExistOptional = userService.findByName(user.getName());
        userNameExistOptional.ifPresent(name -> new RegisterValidator().userNameExist(name, result));

        Optional<User> userEmailExistOptional = userService.findByEmail(user.getEmail());
        userEmailExistOptional.ifPresent(email -> new RegisterValidator().userEmailExist(email, result));

        if (result.hasErrors()) {
            return "registerForm";
        } else {
            userService.saveUser(user);
            return "redirect:/user/login";
        }
    }
}