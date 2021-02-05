package pl.eizodev.app.dto;

import lombok.Builder;
import lombok.Getter;
import pl.eizodev.app.entities.User;

import java.math.BigDecimal;
import java.util.List;

@Builder
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
        return UserDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .stockValue(user.getStockValue())
                .balanceAvailable(user.getBalanceAvailable())
                .walletValue(user.getWalletValue())
                .prevWalletValue(user.getPrevWalletValue())
                .walletPercentageChange(user.getWalletPercentageChange())
                .userStock(UserStockDTO.listOf(user.getUserStock()))
                .build();
    }
}
