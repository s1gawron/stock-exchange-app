package pl.eizodev.app.controllers;

//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.eizodev.app.dao.UserDao;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.validators.RegisterValidator;

@Controller
@RequestMapping("/user")
public class RegisterController {
//    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("newUser", new User());

        return "registerForm";
    }

    @PostMapping("/add-user")
    public String processRegisterForm(@ModelAttribute User user, Model model) {
        String returnPage;
        RegisterValidator registerValidator = new RegisterValidator();

        if (registerValidator.userEmailExists(user.getEmail())) {
            model.addAttribute("messageEmail", "Ten email jest już używany!");
            returnPage = "redirect:/user/register";
        } else if (registerValidator.userNameExists(user.getName())) {
            model.addAttribute("messageName", "Ta nazwa użytkownika jest już zajęta!");
            returnPage = "redirect:/user/register";
        } else {
            UserDao userDao = new UserDao();
//            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            userDao.addUser(user);
            returnPage = "redirect:/stock/mainView";
        }

        return returnPage;
    }
}
