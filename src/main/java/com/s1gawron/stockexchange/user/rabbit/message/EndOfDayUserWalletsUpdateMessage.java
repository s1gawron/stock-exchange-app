package com.s1gawron.stockexchange.user.rabbit.message;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public record EndOfDayUserWalletsUpdateMessage(String timestamp, List<String> usernames) {

    public static EndOfDayUserWalletsUpdateMessage create(final List<String> usernames, final Clock clock) {
        return new EndOfDayUserWalletsUpdateMessage(LocalDateTime.now(clock).toString(), usernames);
    }

}
