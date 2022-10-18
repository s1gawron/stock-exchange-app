package pl.eizodev.app.scheduled;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.eizodev.app.user.rabbit.message.EndOfDayUserWalletsUpdateMessage;
import pl.eizodev.app.user.rabbit.message.EndOfDayUserWalletsUpdateMessagePublisher;
import pl.eizodev.app.user.service.UserService;

import java.time.Clock;
import java.util.List;

@Component
@AllArgsConstructor
public class EndOfDayUserWalletsUpdateJob {

    private static final String EVERY_DAY_AT_MIDNIGHT = "0 0 0 * * *";

    private static final int BATCH_SIZE = 100;

    private final UserService userService;

    private final EndOfDayUserWalletsUpdateMessagePublisher messagePublisher;

    private final Clock clock;

    @Scheduled(cron = EVERY_DAY_AT_MIDNIGHT)
    public void updateUserWalletJob() {
        final List<String> usernames = userService.getAllUsernames();

        Lists.partition(usernames, BATCH_SIZE)
            .forEach(part -> messagePublisher.publishMessage(EndOfDayUserWalletsUpdateMessage.create(part, clock)));
    }

}
