package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.shared.ObjectMapperCreator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

class UserWalletDTOSerializationTest {

    private final ObjectMapper mapper = ObjectMapperCreator.I.getMapper();

    @Test
    @SneakyThrows
    void shouldSerialize() {
        final UserWalletDTO userWalletDTO = new UserWalletDTO(BigDecimal.valueOf(1600.00), BigDecimal.valueOf(10000.00), BigDecimal.valueOf(11600.00),
            BigDecimal.valueOf(10000.00), BigDecimal.valueOf(16.00),
            List.of(
                new UserWalletStockDTO("AAPL", BigDecimal.valueOf(39.25), 10),
                new UserWalletStockDTO("AMZN", BigDecimal.valueOf(40.05), 30)
            ), LocalDateTime.parse("2022-04-12T20:49:11.606847"));
        final String userWalletDTOJsonResult = mapper.writeValueAsString(userWalletDTO);
        final String expectedUserWalletDTOJsonResult = Files.readString(Path.of("src/test/resources/user-wallet.json"));

        final JsonNode expected = mapper.readTree(expectedUserWalletDTOJsonResult);
        final JsonNode result = mapper.readTree(userWalletDTOJsonResult);

        Assertions.assertEquals(expected, result);
    }

}