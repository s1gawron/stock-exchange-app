package com.s1gawron.stockexchange.configuration.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

public class CustomRabbitListenerMessageErrorHandler extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

    private static final Logger log = LoggerFactory.getLogger(CustomRabbitListenerMessageErrorHandler.class);

    @Override public boolean isFatal(final Throwable t) {
        if (t instanceof final ListenerExecutionFailedException failedException) {
            final Message message = failedException.getFailedMessage();
            log.error("Failed to process message from queue {}; message: {}", message.getMessageProperties().getConsumerQueue(), message);
            return false;
        } else {
            log.error("Failed to process message from queue", t);
            return super.isFatal(t);
        }
    }
}
