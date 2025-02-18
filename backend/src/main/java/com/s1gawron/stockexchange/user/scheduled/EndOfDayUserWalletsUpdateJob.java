package com.s1gawron.stockexchange.user.scheduled;

import com.s1gawron.stockexchange.user.service.UserWalletService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class EndOfDayUserWalletsUpdateJob {

    private static final String FROM_MON_TO_SAT_AT_MIDNIGHT = "0 0 0 * * 1-6";

    private static final Logger log = LoggerFactory.getLogger(EndOfDayUserWalletsUpdateJob.class);

    private final ExecutorService endOfDayWalletUpdateExecutor = Executors.newFixedThreadPool(2);

    private final UserWalletService userWalletService;

    public EndOfDayUserWalletsUpdateJob(final UserWalletService userWalletService) {
        this.userWalletService = userWalletService;
    }

    @PreDestroy
    public void shutdownExecutorService() {
        endOfDayWalletUpdateExecutor.shutdown();

        try {
            if (!endOfDayWalletUpdateExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.info("EndOfDayWalletUpdateExecutor did not terminate in the specified time.");
                final List<Runnable> droppedTasks = endOfDayWalletUpdateExecutor.shutdownNow();
                log.info("Executor was abruptly shut down. {} tasks will not be executed.", droppedTasks.size());
            }
        } catch (final InterruptedException e) {
            endOfDayWalletUpdateExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Shutdown interrupted", e);
        }
    }

    @Scheduled(cron = FROM_MON_TO_SAT_AT_MIDNIGHT)
    public void updateUserWalletJob() {
        for (final Long walletId : userWalletService.getAllWalletIds()) {
            endOfDayWalletUpdateExecutor.submit(() -> userWalletService.updateWalletAtTheEndOfTheDay(walletId));
        }
    }

}
