package com.stocksim.marketdata.finnhub;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import com.stocksim.marketdata.PriceTick;
import com.stocksim.marketdata.TickStore;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class FinnhubLiveFeedTest {

	private final TickStore ticks = mock(TickStore.class);
	private final FinnhubLiveFeed feed = new FinnhubLiveFeed(new FinnhubProperties("", "https://x", "wss://x", 55,
			Duration.ZERO, Duration.ZERO, Duration.ZERO, 50), ticks, JsonMapper.builder().build());

	@Test
	void tradeMessagesBecomeTicks() {
		feed.handleMessage("""
				{"type":"trade","data":[
				  {"s":"AAPL","p":229.87,"t":1763560800123,"v":100,"c":["1"]},
				  {"s":"MSFT","p":420.5,"t":1763560800456,"v":5}]}
				""");
		verify(ticks).record(new PriceTick("AAPL", new BigDecimal("229.87"), Instant.ofEpochMilli(1763560800123L)));
		verify(ticks).record(new PriceTick("MSFT", new BigDecimal("420.5"), Instant.ofEpochMilli(1763560800456L)));
	}

	@Test
	void pingAndErrorsAreIgnored() {
		feed.handleMessage("{\"type\":\"ping\"}");
		feed.handleMessage("{\"type\":\"error\",\"msg\":\"Subscribing to too many symbols\"}");
		verify(ticks, never()).record(any());
	}

	@Test
	void notLiveWithoutConnection() {
		feed.setSymbols(java.util.Set.of("AAPL")); // no key -> no connection attempt
		org.assertj.core.api.Assertions.assertThat(feed.isLive("AAPL")).isFalse();
	}
}
