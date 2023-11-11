package com.s1gawron.stockexchange.user.rabbit;

import org.springframework.amqp.core.Queue;

public class EndOfDayUserWalletsUpdateQueue extends Queue {

    public static final String QUEUE_NAME = "end-of-day-user-wallets-update-queue";

    public EndOfDayUserWalletsUpdateQueue() {
        super(QUEUE_NAME);
    }
}
