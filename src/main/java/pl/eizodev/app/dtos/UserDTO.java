package pl.eizodev.app.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.entities.User;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
public class UserDTO {
    private final Long userId;
    private final String name;
    private final BigDecimal stockValue;
    private final BigDecimal balanceAvailable;
    private final BigDecimal walletValue;
    private final BigDecimal prevWalletValue;
    private final BigDecimal walletPercentageChange;
    private final List<UserStockDTO> userStock;

    public static UserDTO of(final User user) {
        return new UserDTO(user.getUserId(), user.getName(), user.getStockValue(), user.getBalanceAvailable(), user.getWalletValue(),
                user.getPrevWalletValue(), user.getWalletPercentageChange(), UserStockDTO.listOf(user.getUserStock()));
    }
}
