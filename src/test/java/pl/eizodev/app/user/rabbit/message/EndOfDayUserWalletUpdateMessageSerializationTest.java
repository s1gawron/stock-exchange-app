package pl.eizodev.app.user.rabbit.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;

class EndOfDayUserWalletUpdateMessageSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void shouldSerialize() {
        final Instant fixedInstant = LocalDateTime.parse("2022-04-13T10:59:15.994144").toInstant(OffsetDateTime.now().getOffset());
        final Clock clock = Clock.fixed(fixedInstant, ZoneId.systemDefault());
        final EndOfDayUserWalletUpdateMessage message = EndOfDayUserWalletUpdateMessage.create("test", clock);
        final String messageJsonResult = mapper.writeValueAsString(message);
        final String expectedMessageJsonResult = Files.readString(Path.of("src/test/resources/end-of-day-user-wallet-update-message.json"));

        final JsonNode expected = mapper.readTree(expectedMessageJsonResult);
        final JsonNode result = mapper.readTree(messageJsonResult);

        Assertions.assertEquals(expected, result);
    }

}