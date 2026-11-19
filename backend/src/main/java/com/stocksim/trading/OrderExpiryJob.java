package com.stocksim.trading;

import java.time.Clock;

import com.stocksim.account.AccountRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Expires DAY orders after the close of their session and gives back what they reserved. */
@Component
class OrderExpiryJob {

	private final OrderRepository orders;
	private final OrderExecutor executor;
	private final AccountRepository accounts;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	OrderExpiryJob(OrderRepository orders, OrderExecutor executor, AccountRepository accounts,
			ApplicationEventPublisher events, Clock clock) {
		this.orders = orders;
		this.executor = executor;
		this.accounts = accounts;
		this.events = events;
		this.clock = clock;
	}

	@Scheduled(fixedDelayString = "${trading.expiry-interval:PT30S}",
			initialDelayString = "${trading.expiry-interval:PT30S}")
	int expireDueOrders() {
		int expired = 0;
		for (long id : orders.dueForExpiry(clock.instant())) {
			var result = executor.expire(id);
			if (result.isPresent()) {
				expired++;
				long accountId = orders.accountIdOf(id).orElseThrow();
				events.publishEvent(new OrderUpdatedEvent(accounts.userIdOf(accountId), result.get()));
			}
		}
		return expired;
	}
}
