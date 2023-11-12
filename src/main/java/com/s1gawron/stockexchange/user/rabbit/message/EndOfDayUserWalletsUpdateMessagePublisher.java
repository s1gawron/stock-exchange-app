package com.s1gawron.stockexchange.user.rabbit.message;

import com.s1gawron.stockexchange.user.rabbit.EndOfDayUserWalletsUpdateDirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EndOfDayUserWalletsUpdateMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public EndOfDayUserWalletsUpdateMessagePublisher(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishMessage(final EndOfDayUserWalletsUpdateMessage message) {
        rabbitTemplate.convertAndSend(EndOfDayUserWalletsUpdateDirectExchange.EXCHANGE_NAME, "", message);
    }

}
