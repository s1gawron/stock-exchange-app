package com.stocksim;

import com.stocksim.marketdata.fake.FakeMarketDataProvider;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Shared base: one Spring context with Postgres + Redis containers and the fake market data provider. Background
 * jobs run only once an hour here, so tests call matcher/expiry methods themselves.
 */
@SpringBootTest(properties = { "marketdata.provider=fake", "marketdata.fake.tick-interval=PT1H",
		"marketdata.subscription-refresh=PT1H", "trading.matcher.tick-interval=PT1H",
		"trading.matcher.sweep-interval=PT1H", "trading.expiry-interval=PT1H" })
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

	@Autowired
	protected FakeMarketDataProvider fakeMarket;

	@Autowired
	protected StringRedisTemplate redis;

	/** Pins the fake price and drops cached quotes/ticks, so the next read sees it. */
	protected void setPrice(String symbol, String price) {
		fakeMarket.pinPrice(symbol, new java.math.BigDecimal(price));
		redis.delete(java.util.List.of("quote:" + symbol, "tick:" + symbol));
	}

	@BeforeEach
	void resetMarket() {
		fakeMarket.reset();
		redis.getConnectionFactory().getConnection().serverCommands().flushDb();
	}
}
