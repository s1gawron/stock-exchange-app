package com.stocksim.marketdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Quotes = vendor REST snapshot (cached in Redis) + the newest live trade on top. For symbols on the live feed the
 * snapshot is only needed for open/previous close, so it is cached much longer and saves vendor calls.
 */
@Service
public class QuoteService {

	private static final String KEY_PREFIX = "quote:";
	private static final Duration LIVE_SNAPSHOT_TTL = Duration.ofMinutes(5);

	private final MarketDataProvider provider;
	private final StringRedisTemplate redis;
	private final MarketDataProperties properties;
	private final TickStore ticks;
	private final LiveFeed liveFeed;

	QuoteService(MarketDataProvider provider, StringRedisTemplate redis, MarketDataProperties properties,
			TickStore ticks, LiveFeed liveFeed) {
		this.provider = provider;
		this.redis = redis;
		this.properties = properties;
		this.ticks = ticks;
		this.liveFeed = liveFeed;
	}

	public Optional<Quote> quote(String symbol) {
		return snapshot(symbol).map(q -> ticks.latest(symbol)
			.filter(t -> t.timestamp().isAfter(q.timestamp()))
			.map(t -> withTick(q, t))
			.orElse(q));
	}

	private Optional<Quote> snapshot(String symbol) {
		String key = KEY_PREFIX + symbol;
		Map<Object, Object> cached = redis.opsForHash().entries(key);
		if (!cached.isEmpty()) {
			return Optional.of(fromHash(symbol, cached));
		}
		Optional<Quote> quote = provider.quote(symbol);
		quote.ifPresent(q -> {
			redis.opsForHash().putAll(key, toHash(q));
			redis.expire(key, liveFeed.isLive(symbol) ? LIVE_SNAPSHOT_TTL : properties.quoteCacheTtl());
		});
		return quote;
	}

	private static Quote withTick(Quote q, PriceTick t) {
		BigDecimal price = t.price().setScale(4, RoundingMode.HALF_UP);
		BigDecimal change = price.subtract(q.previousClose());
		BigDecimal changePct = q.previousClose().signum() == 0 ? BigDecimal.ZERO
				: change.multiply(BigDecimal.valueOf(100)).divide(q.previousClose(), 2, RoundingMode.HALF_UP);
		return new Quote(q.symbol(), price, change, changePct, q.high().max(price), q.low().min(price), q.open(),
				q.previousClose(), t.timestamp());
	}

	private static Map<String, String> toHash(Quote q) {
		Map<String, String> m = new HashMap<>();
		m.put("price", q.price().toPlainString());
		m.put("change", q.change().toPlainString());
		m.put("changePercent", q.changePercent().toPlainString());
		m.put("high", q.high().toPlainString());
		m.put("low", q.low().toPlainString());
		m.put("open", q.open().toPlainString());
		m.put("previousClose", q.previousClose().toPlainString());
		m.put("timestamp", q.timestamp().toString());
		return m;
	}

	private static Quote fromHash(String symbol, Map<Object, Object> m) {
		return new Quote(symbol, dec(m, "price"), dec(m, "change"), dec(m, "changePercent"), dec(m, "high"),
				dec(m, "low"), dec(m, "open"), dec(m, "previousClose"), Instant.parse((String) m.get("timestamp")));
	}

	private static BigDecimal dec(Map<Object, Object> m, String field) {
		return new BigDecimal((String) m.get(field));
	}
}
