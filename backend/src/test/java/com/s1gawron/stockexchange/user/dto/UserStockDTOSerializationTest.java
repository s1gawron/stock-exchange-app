package com.s1gawron.stockexchange.user.dto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserStockDTOSerializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldSerialize() throws IOException {
        final UserStockPositionDTO positionDTO = new UserStockPositionDTO(1L, new BigDecimal("9.5"), BigDecimal.ZERO, new BigDecimal("150.00"),
            LocalDateTime.of(2026, 1, 5, 10, 0), new BigDecimal("665.00"));
        final UserStockDTO userStockDTO = new UserStockDTO("AAPL", "Apple Inc.", "USD", BigDecimal.valueOf(220.00), BigDecimal.valueOf(20.00), 10.00F,
            new BigDecimal("9.5"), BigDecimal.ZERO, BigDecimal.valueOf(150.00), new BigDecimal("665.00"), List.of(positionDTO));

        final String userStockDTOJsonResult = mapper.writeValueAsString(userStockDTO);
        final String expectedUserStockDTOJsonResult = Files.readString(Path.of("src/test/resources/user-stock-dto.json"));

        final JsonNode expected = mapper.readTree(expectedUserStockDTOJsonResult);
        final JsonNode result = mapper.readTree(userStockDTOJsonResult);

        assertEquals(expected, result);
    }

}