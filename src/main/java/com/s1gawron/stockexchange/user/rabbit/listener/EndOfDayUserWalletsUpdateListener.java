package com.s1gawron.stockexchange.user.rabbit.listener;

import com.s1gawron.stockexchange.configuration.rabbit.RabbitBindingConfiguration;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EndOfDayUserWalletsUpdateListener {

    private static final Logger log = LoggerFactory.getLogger(EndOfDayUserWalletsUpdateListener.class);

    private final UserWalletService userWalletService;

    public EndOfDayUserWalletsUpdateListener(final UserWalletService userWalletService) {
        this.userWalletService = userWalletService;
    }

    @RabbitListener(queues = RabbitBindingConfiguration.USER_WALLET_UPDATE_QUEUE_NAME, containerFactory = "customRabbitListenerContainerFactory", concurrency = "1-5")
    public void handle(@Payload final long userId) {
        log.info("Received end of day user wallet update message for user#{}", userId);
        userWalletService.updateUserWalletAtTheEndOfTheDay(userId);
        log.info("User#{} wallet update completed successfully", userId);
    }

}
