package pl.eizodev.app.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import pl.eizodev.app.entities.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterValidator implements Validator {

    @Override
    public boolean supports(final Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(final Object o, final Errors errors) {

        final User user = (User) o;

        ValidationUtils.rejectIfEmpty(errors, "name", "error.userName.empty");
        ValidationUtils.rejectIfEmpty(errors, "email", "error.userEmail.empty");
        ValidationUtils.rejectIfEmpty(errors, "password", "error.userPassword.empty");

        final Pattern pattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$*])(?!.*\\s).{8,12}$");
        final Matcher matcher = pattern.matcher(user.getPassword());

        if (!matcher.matches()) {
            errors.rejectValue("password", "error.invalidPasswordPattern");
        }
    }

    public void userEmailExist(final User user, final Errors errors) {
        if (user != null) {
            errors.rejectValue("email", "error.userEmailExist");
        }
    }

    public void userNameExist(final User user, final Errors errors) {
        if (user != null) {
            errors.rejectValue("name", "error.userNameExist");
        }
    }
}