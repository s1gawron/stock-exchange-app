package pl.eizodev.app.user.rabbit.message;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.eizodev.app.user.rabbit.EndOfDayUserWalletUpdateDirectExchange;

@Service
@AllArgsConstructor
public class EndOfDayUserWalletUpdateMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishMessage(final EndOfDayUserWalletUpdateMessage message) {
        rabbitTemplate.convertAndSend(EndOfDayUserWalletUpdateDirectExchange.EXCHANGE_NAME, "", message);
    }

}
