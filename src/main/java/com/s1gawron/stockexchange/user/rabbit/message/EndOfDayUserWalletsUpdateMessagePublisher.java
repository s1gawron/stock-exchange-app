package com.s1gawron.stockexchange.user.rabbit.message;

import com.s1gawron.stockexchange.user.rabbit.EndOfDayUserWalletsUpdateDirectExchange;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EndOfDayUserWalletsUpdateMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMessage(final EndOfDayUserWalletsUpdateMessage message) {
        rabbitTemplate.convertAndSend(EndOfDayUserWalletsUpdateDirectExchange.EXCHANGE_NAME, "", message);
    }

}
