package pl.eizodev.app.user.rabbit;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import pl.eizodev.app.user.rabbit.message.EndOfDayUserWalletUpdateMessage;
import pl.eizodev.app.user.service.UserWalletService;

@Component
@AllArgsConstructor
public class EndOfDayUserWalletUpdateListener {

    private final UserWalletService userWalletService;

    @RabbitListener(queues = EndOfDayUserWalletUpdateQueue.QUEUE_NAME, containerFactory = "customRabbitListenerContainerFactory", concurrency = "1-5")
    public void handle(@Payload final EndOfDayUserWalletUpdateMessage message) {
        userWalletService.updateUserWalletAtTheEndOfTheDay(message.getUsername());
    }

}
