package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import pl.eizodev.app.entity.Transaction;
import pl.eizodev.app.entity.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        User user = (User) o;

        ValidationUtils.rejectIfEmpty(errors, "name", "error.userName.empty");
        ValidationUtils.rejectIfEmpty(errors, "email", "error.userEmail.empty");
        ValidationUtils.rejectIfEmpty(errors, "password", "error.userPassword.empty");

        Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$*])(?!.*\\s).{8,12}$");
        Matcher matcher = pattern.matcher(user.getPassword());

        if (!matcher.matches()) {
            errors.rejectValue("password", "error.invalidPasswordPattern");
        }
    }

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