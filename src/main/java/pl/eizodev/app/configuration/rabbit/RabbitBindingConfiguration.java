package pl.eizodev.app.configuration.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.eizodev.app.user.rabbit.EndOfDayUserWalletUpdateDirectExchange;
import pl.eizodev.app.user.rabbit.EndOfDayUserWalletUpdateQueue;

import java.util.Map;

@Configuration
public class RabbitBindingConfiguration {

    @Bean
    public EndOfDayUserWalletUpdateQueue endOfDayUserWalletUpdateQueue() {
        return new EndOfDayUserWalletUpdateQueue();
    }

    @Bean
    public EndOfDayUserWalletUpdateDirectExchange endOfDayUserWalletUpdateDirectExchange() {
        return new EndOfDayUserWalletUpdateDirectExchange();
    }

    @Bean
    public Binding endOfDayUserWalletUpdateBinding() {
        return new Binding(EndOfDayUserWalletUpdateQueue.QUEUE_NAME, Binding.DestinationType.QUEUE,
            EndOfDayUserWalletUpdateDirectExchange.EXCHANGE_NAME, "", Map.of());
    }

}
