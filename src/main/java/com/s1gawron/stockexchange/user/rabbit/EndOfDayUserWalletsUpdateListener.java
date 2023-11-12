package com.s1gawron.stockexchange.user.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import com.s1gawron.stockexchange.user.rabbit.message.EndOfDayUserWalletsUpdateMessage;
import com.s1gawron.stockexchange.user.service.UserWalletService;

@Component
public class EndOfDayUserWalletsUpdateListener {

    private final UserWalletService userWalletService;

    public EndOfDayUserWalletsUpdateListener(final UserWalletService userWalletService) {
        this.userWalletService = userWalletService;
    }

    @RabbitListener(queues = EndOfDayUserWalletsUpdateQueue.QUEUE_NAME, containerFactory = "customRabbitListenerContainerFactory", concurrency = "1-5")
    public void handle(@Payload final EndOfDayUserWalletsUpdateMessage message) {
        userWalletService.updateUserWalletsAtTheEndOfTheDay(message.usernames());
    }

}
