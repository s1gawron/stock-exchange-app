package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import pl.eizodev.app.entity.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterValidator {
    public void userEmailExist(User user, Errors errors) {
        if (user != null) {
            errors.rejectValue("email", "error.userEmailExist");
        }
    }

    public void userNameExist(User user, Errors errors) {
        if (user != null) {
            errors.rejectValue("name", "error.userNameExist");
        }
    }

    public void invalidPasswordPattern(User user, Errors errors) {
        Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$*])(?!.*\\s).{8,12}$");
        Matcher matcher = pattern.matcher(user.getPassword());

        if (!matcher.matches()) {
            errors.rejectValue("password", "error.invalidPasswordPattern");
        }
    }
}