package com.s1gawron.stockexchange.user.scheduled;

import com.s1gawron.stockexchange.configuration.RabbitConfiguration;
import com.s1gawron.stockexchange.user.service.UserService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EndOfDayUserWalletsUpdateJob {

    private static final String FROM_MON_TO_FRI_AT_MIDNIGHT = "0 0 0 * * 1-6";

    private final UserService userService;

    private final RabbitTemplate rabbitTemplate;

    public EndOfDayUserWalletsUpdateJob(final UserService userService, final RabbitTemplate rabbitTemplate) {
        this.userService = userService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(cron = FROM_MON_TO_FRI_AT_MIDNIGHT)
    public void updateUserWalletJob() {
        userService.getAllUserIds().forEach(id -> rabbitTemplate.convertAndSend(RabbitConfiguration.USER_WALLET_UPDATE_EXCHANGE,
            RabbitConfiguration.USER_WALLET_UPDATE_QUEUE, id));
    }

}
