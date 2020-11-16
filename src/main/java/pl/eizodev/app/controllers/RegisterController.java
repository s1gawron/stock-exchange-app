package pl.eizodev.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.validators.RegisterValidator;

import java.util.Optional;


@Controller
@RequestMapping("/user")
class RegisterController {

    private final UserRepository userRepository;
    private final UserService userService;

    public RegisterController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
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

        Optional<User> userNameExistOptional = userRepository.findByName(user.getName());
        userNameExistOptional.ifPresent(name -> new RegisterValidator().userNameExist(name, result));

        Optional<User> userEmailExistOptional = userRepository.findByEmail(user.getEmail());
        userEmailExistOptional.ifPresent(email -> new RegisterValidator().userEmailExist(email, result));

        if (result.hasErrors()) {
            return "registerForm";
        } else {
            userService.saveUser(user);
            return "redirect:/user/login";
        }
    }
}