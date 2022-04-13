package pl.eizodev.app.user.rabbit;

import org.springframework.amqp.core.Queue;

public class EndOfDayUserWalletUpdateQueue extends Queue {

    public static final String QUEUE_NAME = "end-of-day-user-wallet-update-queue";

    public EndOfDayUserWalletUpdateQueue() {
        super(QUEUE_NAME);
    }
}
