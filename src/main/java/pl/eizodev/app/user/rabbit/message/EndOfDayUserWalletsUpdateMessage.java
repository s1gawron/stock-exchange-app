package pl.eizodev.app.user.rabbit.message;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonDeserialize(builder = EndOfDayUserWalletsUpdateMessage.EndOfDayUserWalletsUpdateMessageBuilder.class)
public class EndOfDayUserWalletsUpdateMessage {

    private final String timestamp;

    private final List<String> usernames;

    public static EndOfDayUserWalletsUpdateMessage create(final List<String> usernames, final Clock clock) {
        return new EndOfDayUserWalletsUpdateMessage(LocalDateTime.now(clock).toString(), usernames);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class EndOfDayUserWalletsUpdateMessageBuilder {

    }

}
