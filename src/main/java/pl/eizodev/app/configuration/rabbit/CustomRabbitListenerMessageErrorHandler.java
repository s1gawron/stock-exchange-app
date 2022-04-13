package pl.eizodev.app.configuration.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

@Slf4j
public class CustomRabbitListenerMessageErrorHandler extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

    @Override public boolean isFatal(final Throwable t) {
        if (t instanceof ListenerExecutionFailedException) {
            final ListenerExecutionFailedException failedException = (ListenerExecutionFailedException) t;
            final Message message = failedException.getFailedMessage();
            log.error("Failed to process message from queue {}; message: {}", message.getMessageProperties().getConsumerQueue(), message);
            return false;
        } else {
            log.error("Failed to process message from queue", t);
            return super.isFatal(t);
        }
    }
}
