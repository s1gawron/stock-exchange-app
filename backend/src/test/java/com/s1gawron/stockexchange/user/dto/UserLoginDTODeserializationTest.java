package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserLoginDTODeserializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldDeserialize() throws IOException {
        final String userLoginJson = Files.readString(Path.of("src/test/resources/user-login-dto.json"));
        final UserLoginDTO result = mapper.readValue(userLoginJson, UserLoginDTO.class);

        assertEquals("test", result.username());
        assertEquals("Start00!", result.password());
    }

}