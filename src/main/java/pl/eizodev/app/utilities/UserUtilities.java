package pl.eizodev.app.utilities;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtilities {
    public static String getLoggedUser() {
        String username = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            username = authentication.getName();
        }

        return username;
    }
}
