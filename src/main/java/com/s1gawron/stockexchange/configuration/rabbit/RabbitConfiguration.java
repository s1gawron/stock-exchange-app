package com.s1gawron.stockexchange.configuration.rabbit;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableRabbit
@AllArgsConstructor
public class RabbitConfiguration implements RabbitListenerConfigurer {

    private final RabbitProperties rabbitProperties;

    @Bean
    public ConnectionFactory connectionFactory() {
        final CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitProperties.getHost(), rabbitProperties.getPort());

        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        connectionFactory.setChannelCheckoutTimeout(20_000);
        connectionFactory.setConnectionTimeout(20_000);

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setChannelTransacted(true);
        rabbitTemplate.setRetryTemplate(createRetryTemplate());

        return rabbitTemplate;
    }

    @Bean
    public DefaultMessageHandlerMethodFactory customHandlerMethodFactory() {
        final DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(new MappingJackson2MessageConverter());
        return factory;
    }

    @Override public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(customHandlerMethodFactory());
    }

    private RetryTemplate createRetryTemplate() {
        final RetryTemplate retryTemplate = new RetryTemplate();
        final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();

        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(16_000);

        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory customRabbitListenerContainerFactory() {
        final SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        factory.setAdviceChain(createRetryOperationsInterceptor());
        factory.setConnectionFactory(connectionFactory());
        factory.setPrefetchCount(5);
        factory.setRecoveryInterval(10_000L);
        factory.setErrorHandler(errorHandler());

        return factory;
    }

    private RetryOperationsInterceptor createRetryOperationsInterceptor() {
        return RetryInterceptorBuilder.stateless()
            .maxAttempts(10)
            .backOffOptions(1000, 2, 32_000)
            .build();
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(new CustomRabbitListenerMessageErrorHandler());
    }

}

