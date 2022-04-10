package pl.eizodev.app.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Builder
@Getter
@JsonDeserialize(builder = UserWalletDTO.UserWalletDTOBuilder.class)
public class UserWalletDTO {

    private final BigDecimal stockValue;

    private final BigDecimal balanceAvailable;

    private final BigDecimal walletValue;

    private final BigDecimal previousWalletValue;

    private final BigDecimal walletPercentageChange;

    private final List<UserWalletStockDTO> userStock;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserWalletDTOBuilder {

    }
}
