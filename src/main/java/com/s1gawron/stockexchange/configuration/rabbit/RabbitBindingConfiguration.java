package com.s1gawron.stockexchange.configuration.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitBindingConfiguration {

    public static final String USER_WALLET_UPDATE_EXCHANGE_NAME = "end-of-day-user-wallet-update-direct-exchange";

    public static final String USER_WALLET_UPDATE_QUEUE_NAME = "end-of-day-user-wallet-update-queue";

    @Bean
    public Queue endOfDayUserWalletUpdateQueue() {
        return QueueBuilder.durable(USER_WALLET_UPDATE_QUEUE_NAME).build();
    }

    @Bean
    public DirectExchange endOfDayUserWalletUpdateDirectExchange() {
        return new DirectExchange(USER_WALLET_UPDATE_EXCHANGE_NAME);
    }

    @Bean
    public Binding endOfDayUserWalletUpdateBinding() {
        return BindingBuilder.bind(endOfDayUserWalletUpdateQueue())
            .to(endOfDayUserWalletUpdateDirectExchange())
            .with("");
    }

}
