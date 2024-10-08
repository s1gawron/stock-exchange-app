package com.s1gawron.stockexchange.transaction.scheduled;

import com.s1gawron.stockexchange.configuration.RabbitConfiguration;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewTransactionsProcessJob {

    private static final Logger log = LoggerFactory.getLogger(NewTransactionsProcessJob.class);

    private static final String EVERY_15_SEC_FROM_MON_TO_FRI = "*/15 * * * * 1-5";

    private final TransactionService transactionService;

    private final RabbitTemplate rabbitTemplate;

    public NewTransactionsProcessJob(final TransactionService transactionService, final RabbitTemplate rabbitTemplate) {
        this.transactionService = transactionService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(cron = EVERY_15_SEC_FROM_MON_TO_FRI)
    public void processNewTransactionsJob() {
        final List<Long> newTransactionIds = transactionService.getNewTransactionIds();

        if (newTransactionIds.isEmpty()) {
            return;
        }

        log.info("Processing new transactions, size#{}", newTransactionIds.size());

        newTransactionIds.forEach(id -> rabbitTemplate.convertAndSend(RabbitConfiguration.NEW_TRANSACTION_PROCESS_EXCHANGE,
            RabbitConfiguration.NEW_TRANSACTION_PROCESS_QUEUE, id));
        transactionService.changeTransactionsStatus(newTransactionIds, TransactionStatus.IN_PROGRESS);
    }

}
