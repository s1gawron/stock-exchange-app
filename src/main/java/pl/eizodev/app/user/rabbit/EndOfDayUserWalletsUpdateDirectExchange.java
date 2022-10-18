package pl.eizodev.app.user.rabbit;

import org.springframework.amqp.core.DirectExchange;

public class EndOfDayUserWalletsUpdateDirectExchange extends DirectExchange {

    public static final String EXCHANGE_NAME = "end-of-day-user-wallets-update-direct-exchange";

    public EndOfDayUserWalletsUpdateDirectExchange() {
        super(EXCHANGE_NAME);
    }
}
