package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Builder
@Getter
@JsonDeserialize(builder = UserWalletStockDTO.UserWalletStockDTOBuilder.class)
public class UserWalletStockDTO {

    private final String ticker;

    private final BigDecimal averagePurchasePrice;

    private final int quantity;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserWalletStockDTOBuilder {

    }
}
