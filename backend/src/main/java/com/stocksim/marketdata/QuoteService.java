package com.stocksim.marketdata;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Quotes with a short Redis cache in front of the provider, so many users do not multiply vendor calls. */
@Service
public class QuoteService {

	private static final String KEY_PREFIX = "quote:";

	private final MarketDataProvider provider;
	private final StringRedisTemplate redis;
	private final MarketDataProperties properties;

	QuoteService(MarketDataProvider provider, StringRedisTemplate redis, MarketDataProperties properties) {
		this.provider = provider;
		this.redis = redis;
		this.properties = properties;
	}

	public Optional<Quote> quote(String symbol) {
		String key = KEY_PREFIX + symbol;
		Map<Object, Object> cached = redis.opsForHash().entries(key);
		if (!cached.isEmpty()) {
			return Optional.of(fromHash(symbol, cached));
		}
		Optional<Quote> quote = provider.quote(symbol);
		quote.ifPresent(q -> {
			redis.opsForHash().putAll(key, toHash(q));
			redis.expire(key, properties.quoteCacheTtl());
		});
		return quote;
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
