package pl.eizodev.app.user.rabbit;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.eizodev.app.user.rabbit.message.EndOfDayUserWalletsUpdateMessage;
import pl.eizodev.app.user.service.UserWalletService;

@Component
@AllArgsConstructor
public class EndOfDayUserWalletsUpdateListener {

    private final UserWalletService userWalletService;

    @RabbitListener(queues = EndOfDayUserWalletsUpdateQueue.QUEUE_NAME, containerFactory = "customRabbitListenerContainerFactory", concurrency = "1-5")
    public void handle(@Payload final EndOfDayUserWalletsUpdateMessage message) {
        userWalletService.updateUserWalletsAtTheEndOfTheDay(message.getUsernames());
    }

}
