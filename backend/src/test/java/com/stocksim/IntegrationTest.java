package com.stocksim;

import com.stocksim.marketdata.fake.FakeMarketDataProvider;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Shared base: one Spring context with Postgres + Redis containers and the fake market data provider. */
@SpringBootTest(properties = "marketdata.provider=fake")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

	@Autowired
	protected FakeMarketDataProvider fakeMarket;

	@Autowired
	protected StringRedisTemplate redis;

	@BeforeEach
	void resetMarket() {
		fakeMarket.reset();
		redis.getConnectionFactory().getConnection().serverCommands().flushDb();
	}
}
