package com.stocksim.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.stocksim.TestcontainersConfiguration;
import com.stocksim.marketdata.PriceTick;
import com.stocksim.marketdata.TickStore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "marketdata.provider=fake", "marketdata.fake.tick-interval=PT1H",
				"marketdata.subscription-refresh=PT1H", "trading.matcher.tick-interval=PT1H",
				"trading.matcher.sweep-interval=PT1H", "trading.expiry-interval=PT1H",
		"performance.snapshot-cron=-", "performance.catch-up-on-startup=false" })
@Import(TestcontainersConfiguration.class)
class PriceHubWebSocketTest {

	@LocalServerPort
	int port;

	@Autowired
	TickStore ticks;

	@Autowired
	org.springframework.context.ApplicationEventPublisher events;

	@Test
	void loggedInBrowserReceivesSubscribedPrices() throws Exception {
		CookieManager cookies = new CookieManager();
		HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).build();
		String base = "http://localhost:" + port;
		http.send(HttpRequest.newBuilder(URI.create(base + "/api/auth/csrf")).build(), HttpResponse.BodyHandlers.discarding());
		String xsrf = cookies.getCookieStore().getCookies().stream()
			.filter(c -> c.getName().equals("XSRF-TOKEN")).findFirst().orElseThrow().getValue();
		var register = http.send(HttpRequest.newBuilder(URI.create(base + "/api/auth/register"))
			.header("Content-Type", "application/json")
			.header("X-XSRF-TOKEN", xsrf)
			.POST(HttpRequest.BodyPublishers.ofString("""
					{"email":"%s@test.local","displayName":"WS","password":"correct-horse"}
					""".formatted(UUID.randomUUID())))
			.build(), HttpResponse.BodyHandlers.ofString());
		assertThat(register.statusCode()).isEqualTo(201);
		// Session cookie (Spring Session in Redis) must not be readable by scripts and not sent cross-site.
		assertThat(register.headers().allValues("Set-Cookie")).anySatisfy(c -> assertThat(c)
			.startsWith("SESSION=").contains("HttpOnly").contains("SameSite=Lax"));

		String cookieHeader = cookies.getCookieStore().getCookies().stream()
			.map(c -> c.getName() + "=" + c.getValue()).collect(Collectors.joining("; "));
		BlockingQueue<String> messages = new LinkedBlockingQueue<>();
		WebSocket ws = HttpClient.newHttpClient().newWebSocketBuilder()
			.header("Cookie", cookieHeader)
			.buildAsync(URI.create("ws://localhost:" + port + "/ws"), new WebSocket.Listener() {
				@Override
				public CompletionStage<?> onText(WebSocket w, CharSequence data, boolean last) {
					messages.add(data.toString());
					w.request(1);
					return null;
				}
			}).get(5, TimeUnit.SECONDS);

		ws.sendText("{\"type\":\"subscribe\",\"symbols\":[\"AAPL\"]}", true).get(5, TimeUnit.SECONDS);
		Thread.sleep(200); // let the server apply the subscription
		ticks.record(new PriceTick("MSFT", new BigDecimal("400"), Instant.now())); // not subscribed
		ticks.record(new PriceTick("AAPL", new BigDecimal("231.5"), Instant.now()));

		String msg = messages.poll(5, TimeUnit.SECONDS);
		assertThat(msg).contains("\"type\":\"price\"").contains("\"symbol\":\"AAPL\"").contains("231.5")
			.contains("\"session\":");
		assertThat(messages.poll(1, TimeUnit.SECONDS)).isNull(); // no MSFT

		// Order updates go only to the order's owner.
		String me = http.send(HttpRequest.newBuilder(URI.create(base + "/api/auth/me")).build(),
				HttpResponse.BodyHandlers.ofString()).body();
		long myId = Long.parseLong(me.replaceAll(".*\"id\":(\\d+).*", "$1"));
		var order = new com.stocksim.trading.OrderView(42, "AAPL", com.stocksim.trading.OrderSide.BUY,
				com.stocksim.trading.OrderType.LIMIT, BigDecimal.ONE, new BigDecimal("200"), null,
				com.stocksim.trading.TimeInForce.GTC, com.stocksim.trading.OrderStatus.FILLED, null, BigDecimal.ZERO,
				BigDecimal.ZERO, null, Instant.now(), Instant.now(), null);
		events.publishEvent(new com.stocksim.trading.OrderUpdatedEvent(myId + 1000, order));
		events.publishEvent(new com.stocksim.trading.OrderUpdatedEvent(myId, order));
		String orderMsg = messages.poll(5, TimeUnit.SECONDS);
		assertThat(orderMsg).contains("\"type\":\"order\"").contains("\"id\":42");
		assertThat(messages.poll(1, TimeUnit.SECONDS)).isNull(); // the other user's event was not sent
		ws.abort();
	}

	@Test
	void anonymousHandshakeIsRefused() {
		assertThatThrownBy(() -> HttpClient.newHttpClient().newWebSocketBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.buildAsync(URI.create("ws://localhost:" + port + "/ws"), new WebSocket.Listener() {
			}).get(5, TimeUnit.SECONDS)).rootCause().hasMessageContaining("401");
	}
}
