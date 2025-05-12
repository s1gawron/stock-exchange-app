package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRegisterDTODeserializationTest {

    private final ObjectMapper mapper = ObjectMapperFactory.I.getMapper();

    @Test
    void shouldDeserialize() throws IOException {
        final String userRegisterJson = Files.readString(Path.of("src/test/resources/user-register-dto.json"));
        final UserRegisterDTO result = mapper.readValue(userRegisterJson, UserRegisterDTO.class);

        assertEquals("test", result.username());
        assertEquals("test@test.pl", result.email());
        assertEquals("Start00!", result.password());
    }

}