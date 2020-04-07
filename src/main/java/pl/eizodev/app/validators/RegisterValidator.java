package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import pl.eizodev.app.entity.User;

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
}