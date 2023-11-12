package com.s1gawron.stockexchange.scheduled;

import com.google.common.collect.Lists;
import com.s1gawron.stockexchange.user.rabbit.message.EndOfDayUserWalletsUpdateMessage;
import com.s1gawron.stockexchange.user.rabbit.message.EndOfDayUserWalletsUpdateMessagePublisher;
import com.s1gawron.stockexchange.user.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Component
public class EndOfDayUserWalletsUpdateJob {

    private static final String EVERY_DAY_AT_MIDNIGHT = "0 0 0 * * *";

    private static final int BATCH_SIZE = 100;

    private final UserService userService;

    private final EndOfDayUserWalletsUpdateMessagePublisher messagePublisher;

    private final Clock clock;

    public EndOfDayUserWalletsUpdateJob(final UserService userService, final EndOfDayUserWalletsUpdateMessagePublisher messagePublisher, final Clock clock) {
        this.userService = userService;
        this.messagePublisher = messagePublisher;
        this.clock = clock;
    }

    @Scheduled(cron = EVERY_DAY_AT_MIDNIGHT)
    public void updateUserWalletJob() {
        final List<String> usernames = userService.getAllUsernames();

        Lists.partition(usernames, BATCH_SIZE)
            .forEach(part -> messagePublisher.publishMessage(EndOfDayUserWalletsUpdateMessage.create(part, clock)));
    }

}
