package pl.eizodev.app.user.rabbit.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonDeserialize(builder = EndOfDayUserWalletUpdateMessage.EndOfDayUserWalletUpdateMessageBuilder.class)
public class EndOfDayUserWalletUpdateMessage {

    private final String timestamp;

    private final String username;

    public static EndOfDayUserWalletUpdateMessage create(final String username, final Clock clock) {
        return new EndOfDayUserWalletUpdateMessage(LocalDateTime.now(clock).toString(), username);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class EndOfDayUserWalletUpdateMessageBuilder {

    }

}
