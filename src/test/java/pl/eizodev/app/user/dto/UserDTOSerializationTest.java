package pl.eizodev.app.user.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

class UserDTOSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldSerialize() {
        final UserWalletDTO userWalletDTO = new UserWalletDTO(new BigDecimal("0"), new BigDecimal("10000"), new BigDecimal("11600"), new BigDecimal("10000"),
            new BigDecimal("16.0"),
            List.of(
                new UserWalletStockDTO("AAPL", BigDecimal.valueOf(39.25), 10),
                new UserWalletStockDTO("AMZN", BigDecimal.valueOf(40.05), 30)
            ), LocalDateTime.parse("2022-04-12T20:49:11.283736"));
        final UserDTO userDTO = new UserDTO("test", "test@test.pl", userWalletDTO);
        final String userDTOJsonResult = mapper.writeValueAsString(userDTO);
        final String expectedUserDTOJsonResult = Files.readString(Path.of("src/test/resources/user-dto.json"));

        final JsonNode expected = mapper.readTree(expectedUserDTOJsonResult);
        final JsonNode result = mapper.readTree(userDTOJsonResult);

        Assertions.assertEquals(expected, result);
    }

}