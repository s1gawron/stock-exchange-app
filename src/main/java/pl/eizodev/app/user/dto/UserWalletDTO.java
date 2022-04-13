package pl.eizodev.app.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime lastUpdateDate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserWalletDTOBuilder {

    }
}
