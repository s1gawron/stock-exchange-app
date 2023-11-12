package com.s1gawron.stockexchange.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.shared.ObjectMapperCreator;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserLoginDTODeserializationTest {

    private final ObjectMapper mapper = ObjectMapperCreator.I.getMapper();

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        final String userLoginJson = Files.readString(Path.of("src/test/resources/user-login.json"));
        final UserLoginDTO result = mapper.readValue(userLoginJson, UserLoginDTO.class);

        assertEquals("test", result.username());
        assertEquals("Start00!", result.password());
    }

}