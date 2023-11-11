package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserLoginDTODeserializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        final String userLoginJson = Files.readString(Path.of("src/test/resources/user-login.json"));
        final UserLoginDTO result = mapper.readValue(userLoginJson, UserLoginDTO.class);

        assertEquals("test", result.getUsername());
        assertEquals("Start00!", result.getPassword());
    }

}