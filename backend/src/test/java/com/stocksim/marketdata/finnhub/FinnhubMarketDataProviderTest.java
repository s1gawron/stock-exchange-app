package com.stocksim.marketdata.finnhub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.stocksim.marketdata.MarketDataProperties;
import com.stocksim.marketdata.MarketDataUnavailableException;
import com.stocksim.marketdata.UsMarketCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class FinnhubMarketDataProviderTest {

	private static final String BASE = "https://finnhub.test/api/v1";

	// Thursday 2026-11-19 10:00 New York (regular session per local calendar)
	private static final Instant THU_10AM_NY = Instant.parse("2026-11-19T15:00:00Z");

	private MockRestServiceServer server;
	private FinnhubMarketDataProvider provider;
	private FinnhubMarketStatus marketStatus;
	private UsMarketCalendar calendar;
	private MutableClock clock;

	static final class MutableClock extends Clock {
		Instant now;

		MutableClock(Instant now) {
			this.now = now;
		}

		@Override
		public java.time.ZoneId getZone() {
			return java.time.ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(java.time.ZoneId zone) {
			return Clock.fixed(now, zone);
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	private static FinnhubProperties props(String key) {
		return new FinnhubProperties(key, BASE, "wss://x", 600, Duration.ofMillis(10), Duration.ofSeconds(1),
				Duration.ofSeconds(60));
	}

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl(BASE).defaultHeader("X-Finnhub-Token", "secret");
		server = MockRestServiceServer.bindTo(builder).build();
		clock = new MutableClock(THU_10AM_NY);
		calendar = new UsMarketCalendar(clock, new MarketDataProperties.UsCalendar(List.of(), List.of()));
		var client = new FinnhubClient(builder.build(), props("secret"));
		marketStatus = new FinnhubMarketStatus(client, calendar, clock, Duration.ofSeconds(60));
		provider = new FinnhubMarketDataProvider(client, marketStatus);
	}

	private static String status(boolean isOpen, String session, String holiday) {
		return """
				{"exchange":"US","holiday":%s,"isOpen":%s,"session":%s,"t":1795090084,"timezone":"America/New_York"}
				""".formatted(holiday == null ? "null" : "\"" + holiday + "\"", isOpen,
				session == null ? "null" : "\"" + session + "\"");
	}

	@Test
	void marketStatusComesFromFinnhubAndIsCached() {
		server.expect(org.springframework.test.web.client.ExpectedCount.once(),
				requestTo(BASE + "/stock/market-status?exchange=US"))
			.andRespond(withSuccess(status(false, "pre-market", null), MediaType.APPLICATION_JSON));

		// Local calendar says open at 10:00 NY; Finnhub says closed and wins.
		assertThat(provider.marketStatus()).isEqualTo(new com.stocksim.marketdata.MarketStatus(false, "Closed (pre-market)"));
		clock.now = THU_10AM_NY.plusSeconds(59);
		assertThat(provider.marketStatus().open()).isFalse(); // from cache, no second call
		server.verify();
	}

	@Test
	void marketStatusMapping() {
		var open = new FinnhubClient.MarketStatusResponse("US", null, true, "regular", 1L, "America/New_York");
		var postWithOpenFlag = new FinnhubClient.MarketStatusResponse("US", null, true, "post-market", 1L, null);
		var holiday = new FinnhubClient.MarketStatusResponse("US", "Thanksgiving Day", false, null, 1L, null);

		assertThat(FinnhubMarketStatus.map(open).open()).isTrue();
		assertThat(FinnhubMarketStatus.map(postWithOpenFlag).open()).isFalse();
		assertThat(FinnhubMarketStatus.map(holiday).description()).isEqualTo("Closed (Thanksgiving Day)");
	}

	@Test
	void marketStatusFallsBackToLocalCalendarWhenFinnhubFails() {
		server.expect(requestTo(BASE + "/stock/market-status?exchange=US"))
			.andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

		assertThat(provider.marketStatus()).isEqualTo(new com.stocksim.marketdata.MarketStatus(true, "Open"));
	}

	@Test
	void holidaysFromFinnhubFeedTheFallbackCalendar() {
		server.expect(requestTo(BASE + "/stock/market-holiday?exchange=US")).andRespond(withSuccess("""
				{"data":[{"eventName":"Thanksgiving Day","atDate":"2026-11-19","tradingHour":"","postMarket":""},
				         {"eventName":"Day after Thanksgiving","atDate":"2026-11-20","tradingHour":"09:30-13:00","postMarket":"13:00-17:00"},
				         {"eventName":"Broken","atDate":"not-a-date","tradingHour":""}],
				 "exchange":"US","timezone":"America/New_York"}
				""", MediaType.APPLICATION_JSON));

		assertThat(marketStatus.refreshHolidays()).isEqualTo(2);
		assertThat(calendar.status().description()).isEqualTo("Closed (holiday)");
		clock.now = Instant.parse("2026-11-20T17:59:00Z"); // 12:59 NY on early-close day
		assertThat(calendar.status().open()).isTrue();
		clock.now = Instant.parse("2026-11-20T18:00:00Z"); // 13:00 NY
		assertThat(calendar.status().open()).isFalse();
	}

	@Test
	void mapsQuoteAndSendsTokenInHeaderNotUrl() {
		server.expect(requestTo(BASE + "/quote?symbol=AAPL"))
			.andExpect(header("X-Finnhub-Token", "secret"))
			.andRespond(withSuccess("""
					{"c":229.87,"d":1.5,"dp":0.6567,"h":231.0,"l":227.5,"o":228.1,"pc":228.37,"t":1763560800}
					""", MediaType.APPLICATION_JSON));

		var quote = provider.quote("AAPL").orElseThrow();

		assertThat(quote.price()).isEqualByComparingTo(new BigDecimal("229.87"));
		assertThat(quote.changePercent()).isEqualByComparingTo(new BigDecimal("0.66"));
		assertThat(quote.timestamp()).isEqualTo(Instant.ofEpochSecond(1763560800));
		server.verify();
	}

	@Test
	void unknownSymbolReturnsEmpty() {
		server.expect(requestTo(BASE + "/quote?symbol=NOPE"))
			.andRespond(withSuccess("""
					{"c":0,"d":null,"dp":null,"h":0,"l":0,"o":0,"pc":0,"t":0}
					""", MediaType.APPLICATION_JSON));

		assertThat(provider.quote("NOPE")).isEmpty();
	}

	@Test
	void vendorErrorBecomesMarketDataUnavailable() {
		server.expect(requestTo(BASE + "/quote?symbol=AAPL")).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

		assertThatThrownBy(() -> provider.quote("AAPL")).isInstanceOf(MarketDataUnavailableException.class)
			.hasMessageContaining("429");
	}

	@Test
	void mapsSymbolList() {
		server.expect(requestTo(BASE + "/stock/symbol?exchange=US")).andRespond(withSuccess("""
				[{"currency":"USD","description":"APPLE INC","displaySymbol":"AAPL","figi":"BBG000B9XRY4",
				  "mic":"XNAS","symbol":"AAPL","type":"Common Stock"}]
				""", MediaType.APPLICATION_JSON));

		assertThat(provider.listInstruments()).singleElement()
			.satisfies(l -> {
				assertThat(l.symbol()).isEqualTo("AAPL");
				assertThat(l.name()).isEqualTo("APPLE INC");
				assertThat(l.type()).isEqualTo("Common Stock");
			});
	}

	@Test
	void missingApiKeyFailsWithoutCallingVendor() {
		var p = new FinnhubMarketDataProvider(new FinnhubClient(RestClient.create(), props("")), null);

		assertThatThrownBy(() -> p.quote("AAPL")).isInstanceOf(MarketDataUnavailableException.class);
	}

	@Test
	void rateLimiterRefusesWhenBucketIsEmpty() {
		var limiter = new RateLimiter(2);
		assertThat(limiter.tryAcquire(Duration.ZERO)).isTrue();
		assertThat(limiter.tryAcquire(Duration.ZERO)).isTrue();
		assertThat(limiter.tryAcquire(Duration.ZERO)).isFalse();
	}

	@Test
	void apiKeyIsMaskedInToString() {
		assertThat(props("secret").toString()).doesNotContain("secret").contains("***");
	}
}
