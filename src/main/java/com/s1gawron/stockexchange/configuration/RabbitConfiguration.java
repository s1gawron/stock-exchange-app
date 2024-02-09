package com.s1gawron.stockexchange.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    public static final String USER_WALLET_UPDATE_QUEUE = "end-of-day-user-wallet-update-queue";

    public static final String USER_WALLET_UPDATE_EXCHANGE = "end-of-day-user-wallet-update-direct-exchange";

    public static final String USER_WALLET_UPDATE_DEAD_LETTER_QUEUE = "end-of-day-user-wallet-update-dead-letter-queue";

    private static final String DEAD_LETTER_EXCHANGE_ARGUMENT = "x-dead-letter-exchange";

    private static final String USER_WALLET_UPDATE_DEAD_LETTER_EXCHANGE = USER_WALLET_UPDATE_QUEUE + ".dlx";

    public static final String NEW_TRANSACTION_PROCESS_QUEUE = "new-transaction-process-queue";

    public static final String NEW_TRANSACTION_PROCESS_EXCHANGE = "new-transaction-process-direct-exchange";

    public static final String NEW_TRANSACTION_PROCESS_DEAD_LETTER_QUEUE = "new-transaction-process-dead-letter-queue";

    private static final String NEW_TRANSACTION_PROCESS_DEAD_LETTER_EXCHANGE = NEW_TRANSACTION_PROCESS_QUEUE + ".dlx";

    @Bean
    public Queue endOfDayUserWalletUpdateQueue() {
        return QueueBuilder.durable(USER_WALLET_UPDATE_QUEUE)
            .withArgument(DEAD_LETTER_EXCHANGE_ARGUMENT, USER_WALLET_UPDATE_DEAD_LETTER_EXCHANGE)
            .build();
    }

    @Bean
    public DirectExchange endOfDayUserWalletUpdateDirectExchange() {
        return new DirectExchange(USER_WALLET_UPDATE_EXCHANGE);
    }

    @Bean
    public Binding endOfDayUserWalletUpdateBinding() {
        return BindingBuilder.bind(endOfDayUserWalletUpdateQueue())
            .to(endOfDayUserWalletUpdateDirectExchange())
            .with(USER_WALLET_UPDATE_QUEUE);
    }

    @Bean
    public Queue userWalletUpdateDeadLetterQueue() {
        return QueueBuilder.durable(USER_WALLET_UPDATE_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public FanoutExchange userWalletUpdateDeadLetterExchange() {
        return new FanoutExchange(USER_WALLET_UPDATE_DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding userWalletUpdateDeadLetterBinding() {
        return BindingBuilder.bind(userWalletUpdateDeadLetterQueue()).to(userWalletUpdateDeadLetterExchange());
    }

    @Bean
    public Queue newTransactionProcessQueue() {
        return QueueBuilder.durable(NEW_TRANSACTION_PROCESS_QUEUE)
            .withArgument(DEAD_LETTER_EXCHANGE_ARGUMENT, NEW_TRANSACTION_PROCESS_DEAD_LETTER_EXCHANGE)
            .build();
    }

    @Bean
    public DirectExchange newTransactionProcessDirectExchange() {
        return new DirectExchange(NEW_TRANSACTION_PROCESS_EXCHANGE);
    }

    @Bean
    public Binding newTransactionProcessBinding() {
        return BindingBuilder.bind(newTransactionProcessQueue())
            .to(newTransactionProcessDirectExchange())
            .with(NEW_TRANSACTION_PROCESS_QUEUE);
    }

    @Bean
    public Queue newTransactionProcessDeadLetterQueue() {
        return QueueBuilder.durable(NEW_TRANSACTION_PROCESS_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public FanoutExchange newTransactionProcessDeadLetterExchange() {
        return new FanoutExchange(NEW_TRANSACTION_PROCESS_DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding newTransactionProcessDeadLetterBinding() {
        return BindingBuilder.bind(newTransactionProcessDeadLetterQueue()).to(newTransactionProcessDeadLetterExchange());
    }

}
