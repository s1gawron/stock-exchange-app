package pl.eizodev.app.user.rabbit.message;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.eizodev.app.user.rabbit.EndOfDayUserWalletsUpdateDirectExchange;

@Service
@AllArgsConstructor
public class EndOfDayUserWalletsUpdateMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMessage(final EndOfDayUserWalletsUpdateMessage message) {
        rabbitTemplate.convertAndSend(EndOfDayUserWalletsUpdateDirectExchange.EXCHANGE_NAME, "", message);
    }

}
