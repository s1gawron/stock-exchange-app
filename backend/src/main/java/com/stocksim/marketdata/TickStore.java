package com.stocksim.marketdata;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Latest trade per symbol in Redis ({@code tick:SYMBOL}); publishes each new tick as a {@link PriceTick} event. */
@Component
public class TickStore {

	private static final String PREFIX = "tick:";
	private static final Duration TTL = Duration.ofDays(4);

	private final StringRedisTemplate redis;
	private final ApplicationEventPublisher events;

	TickStore(StringRedisTemplate redis, ApplicationEventPublisher events) {
		this.redis = redis;
		this.events = events;
	}

	public void record(PriceTick tick) {
		Optional<PriceTick> current = latest(tick.symbol());
		if (current.isPresent() && current.get().timestamp().isAfter(tick.timestamp())) {
			return; // out-of-order trade, keep the newer one
		}
		String key = PREFIX + tick.symbol();
		redis.opsForHash().putAll(key, Map.of("price", tick.price().toPlainString(), "ts", tick.timestamp().toString()));
		redis.expire(key, TTL);
		events.publishEvent(tick);
	}

	public Optional<PriceTick> latest(String symbol) {
		Map<Object, Object> m = redis.opsForHash().entries(PREFIX + symbol);
		if (m.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(new PriceTick(symbol, new BigDecimal((String) m.get("price")),
				Instant.parse((String) m.get("ts"))));
	}
}
