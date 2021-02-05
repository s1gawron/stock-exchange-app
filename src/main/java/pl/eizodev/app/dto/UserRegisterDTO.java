package pl.eizodev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.entities.User;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class UserRegisterDTO {
    private final String name;
    private final String email;
    private final String password;
    private final BigDecimal balanceAvailable;

    public static UserRegisterDTO of(final User user) {
        return new UserRegisterDTO(user.getName(), user.getEmail(), user.getPassword(), user.getBalanceAvailable());
    }
}
