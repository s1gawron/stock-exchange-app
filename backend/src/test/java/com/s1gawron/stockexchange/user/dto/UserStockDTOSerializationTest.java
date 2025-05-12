package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserStockDTOSerializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldSerialize() throws IOException {
        final UserStockDTO userStockDTO = new UserStockDTO("AAPL", "Apple Inc.", BigDecimal.valueOf(220.00), BigDecimal.valueOf(20.00), 10.00F, 9,
            0, BigDecimal.valueOf(150.00), BigDecimal.valueOf(630.00));

        final String userStockDTOJsonResult = mapper.writeValueAsString(userStockDTO);
        final String expectedUserStockDTOJsonResult = Files.readString(Path.of("src/test/resources/user-stock-dto.json"));

        final JsonNode expected = mapper.readTree(expectedUserStockDTOJsonResult);
        final JsonNode result = mapper.readTree(userStockDTOJsonResult);

        assertEquals(expected, result);
    }

}