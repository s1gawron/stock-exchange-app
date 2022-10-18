package pl.eizodev.app.configuration.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.eizodev.app.user.rabbit.EndOfDayUserWalletsUpdateDirectExchange;
import pl.eizodev.app.user.rabbit.EndOfDayUserWalletsUpdateQueue;

import java.util.Map;

@Configuration
public class RabbitBindingConfiguration {

    @Bean
    public EndOfDayUserWalletsUpdateQueue endOfDayUserWalletUpdateQueue() {
        return new EndOfDayUserWalletsUpdateQueue();
    }

    @Bean
    public EndOfDayUserWalletsUpdateDirectExchange endOfDayUserWalletUpdateDirectExchange() {
        return new EndOfDayUserWalletsUpdateDirectExchange();
    }

    @Bean
    public Binding endOfDayUserWalletUpdateBinding() {
        return new Binding(EndOfDayUserWalletsUpdateQueue.QUEUE_NAME, Binding.DestinationType.QUEUE,
            EndOfDayUserWalletsUpdateDirectExchange.EXCHANGE_NAME, "", Map.of());
    }

}
