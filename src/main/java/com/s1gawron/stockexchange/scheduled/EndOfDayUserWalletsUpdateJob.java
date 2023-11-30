package com.s1gawron.stockexchange.scheduled;

import com.s1gawron.stockexchange.configuration.rabbit.RabbitBindingConfiguration;
import com.s1gawron.stockexchange.user.service.UserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EndOfDayUserWalletsUpdateJob {

    private static final String EVERY_DAY_AT_MIDNIGHT = "0 0 0 * * *";

    private final UserService userService;

    private final RabbitTemplate rabbitTemplate;

    public EndOfDayUserWalletsUpdateJob(final UserService userService, final RabbitTemplate rabbitTemplate) {
        this.userService = userService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(cron = EVERY_DAY_AT_MIDNIGHT)
    public void updateUserWalletJob() {
        userService.getAllUserIds().forEach(id -> rabbitTemplate.convertAndSend(RabbitBindingConfiguration.USER_WALLET_UPDATE_EXCHANGE_NAME,
            RabbitBindingConfiguration.USER_WALLET_UPDATE_QUEUE_NAME, id));
    }

}
