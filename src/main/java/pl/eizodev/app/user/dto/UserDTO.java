package pl.eizodev.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.stock.dto.UserStockDTO;
import pl.eizodev.app.user.User;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
public class UserDTO {
    private final String name;
    private final BigDecimal stockValue;
    private final BigDecimal balanceAvailable;
    private final BigDecimal walletValue;
    private final BigDecimal prevWalletValue;
    private final BigDecimal walletPercentageChange;
    private final List<UserStockDTO> userStock;

    public static UserDTO of(final User user) {
        return new UserDTO(user.getName(), user.getStockValue(), user.getBalanceAvailable(), user.getWalletValue(),
                user.getPrevWalletValue(), user.getWalletPercentageChange(), UserStockDTO.listOf(user.getUserStock()));
    }
}
