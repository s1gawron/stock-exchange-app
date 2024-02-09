package com.s1gawron.stockexchange.transaction.rabbit.listener;

import com.s1gawron.stockexchange.configuration.RabbitConfiguration;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewTransactionsProcessListener {

    private static final Logger log = LoggerFactory.getLogger(NewTransactionsProcessListener.class);

    private final TransactionService transactionService;

    public NewTransactionsProcessListener(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @RabbitListener(queues = RabbitConfiguration.NEW_TRANSACTION_PROCESS_QUEUE, concurrency = "1-5")
    public void handle(@Payload final long transactionId) {
        log.info("Received new transaction process message for transaction#{}", transactionId);
        transactionService.processTransaction(transactionId);
        log.info("New transaction#{} process job completed successfully", transactionId);
    }

    @RabbitListener(queues = RabbitConfiguration.NEW_TRANSACTION_PROCESS_DEAD_LETTER_QUEUE)
    public void handleDeadLetterQueue(@Payload final long failedTransactionId) {
        log.info("Changing failed transaction#{} status to new", failedTransactionId);
        transactionService.changeTransactionsStatus(List.of(failedTransactionId), TransactionStatus.NEW);
    }

}
