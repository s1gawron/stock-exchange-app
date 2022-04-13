package pl.eizodev.app.scheduler;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.eizodev.app.user.rabbit.message.EndOfDayUserWalletUpdateMessage;
import pl.eizodev.app.user.rabbit.message.EndOfDayUserWalletUpdateMessagePublisher;
import pl.eizodev.app.user.service.UserService;

import java.time.Clock;

@Component
@AllArgsConstructor
public class EndOfDayUserWalletUpdateJob {

    private static final String EVERY_DAY_AT_11_PM = "0 0 23 * * *";

    private final UserService userService;

    private final EndOfDayUserWalletUpdateMessagePublisher messagePublisher;

    private final Clock clock;

    @Scheduled(cron = EVERY_DAY_AT_11_PM)
    public void updateUserWalletJob() {
        userService.getAllUsernames().forEach(username -> messagePublisher.publishMessage(EndOfDayUserWalletUpdateMessage.create(username, clock)));
    }

}
