package pl.eizodev.app.user.rabbit;

import org.springframework.amqp.core.DirectExchange;

public class EndOfDayUserWalletUpdateDirectExchange extends DirectExchange {

    public static final String EXCHANGE_NAME = "end-of-day-user-wallet-update-direct-exchange";

    public EndOfDayUserWalletUpdateDirectExchange() {
        super(EXCHANGE_NAME);
    }
}
