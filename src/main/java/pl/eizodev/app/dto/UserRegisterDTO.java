package pl.eizodev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class UserRegisterDTO {
    private final String name;
    private final String email;
    private final String password;
    private final BigDecimal balanceAvailable;
}
