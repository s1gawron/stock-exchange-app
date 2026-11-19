package com.stocksim.marketdata.finnhub;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.stocksim.marketdata.LiveFeed;
import com.stocksim.marketdata.PriceTick;
import com.stocksim.marketdata.TickStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One WebSocket connection to Finnhub for the whole app. Reconnects with backoff and re-subscribes.
 * The token must be in the URL (Finnhub WS API); the URL is never logged.
 */
class FinnhubLiveFeed implements LiveFeed, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(FinnhubLiveFeed.class);

	private final FinnhubProperties properties;
	private final TickStore ticks;
	private final JsonMapper json;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r, "finnhub-ws");
		t.setDaemon(true);
		return t;
	});

	private final Object lock = new Object();
	private Set<String> wanted = Set.of();
	private Set<String> subscribed = new HashSet<>();
	private WebSocket socket;
	private boolean connecting;
	private long backoffSeconds = 1;
	private volatile boolean stopped;

	FinnhubLiveFeed(FinnhubProperties properties, TickStore ticks, JsonMapper json) {
		this.properties = properties;
		this.ticks = ticks;
		this.json = json;
	}

	@Override
	public int maxSymbols() {
		return properties.websocketMaxSymbols();
	}

	@Override
	public boolean isLive(String symbol) {
		synchronized (lock) {
			return socket != null && subscribed.contains(symbol);
		}
	}

	@Override
	public void setSymbols(Set<String> symbols) {
		synchronized (lock) {
			wanted = Set.copyOf(symbols);
			if (socket == null) {
				if (!wanted.isEmpty()) {
					connect();
				}
				return;
			}
			syncSubscriptions();
		}
	}

	/** Must hold {@link #lock}. */
	private void syncSubscriptions() {
		for (String s : Set.copyOf(subscribed)) {
			if (!wanted.contains(s)) {
				send("{\"type\":\"unsubscribe\",\"symbol\":\"" + s + "\"}");
				subscribed.remove(s);
			}
		}
		for (String s : wanted) {
			if (!subscribed.contains(s)) {
				send("{\"type\":\"subscribe\",\"symbol\":\"" + s + "\"}");
				subscribed.add(s);
			}
		}
	}

	private void send(String text) {
		socket.sendText(text, true);
	}

	/** Must hold {@link #lock}. */
	private void connect() {
		if (connecting || stopped || !properties.isConfigured()) {
			return;
		}
		connecting = true;
		URI uri = URI.create(properties.websocketUrl() + "?token="
				+ URLEncoder.encode(properties.apiKey(), StandardCharsets.UTF_8));
		http.newWebSocketBuilder().buildAsync(uri, new Listener()).whenComplete((ws, error) -> {
			synchronized (lock) {
				connecting = false;
				if (error != null) {
					// Do not log the exception message: it may contain the URL with the token.
					log.warn("Finnhub WebSocket connect failed ({}); retry in {}s", error.getClass().getSimpleName(),
							backoffSeconds);
					scheduleReconnect();
					return;
				}
				socket = ws;
				subscribed = new HashSet<>();
				backoffSeconds = 1;
				log.info("Finnhub WebSocket connected");
				syncSubscriptions();
			}
		});
	}

	/** Must hold {@link #lock}. */
	private void scheduleReconnect() {
		if (stopped) {
			return;
		}
		long delay = backoffSeconds;
		backoffSeconds = Math.min(backoffSeconds * 2, 60);
		scheduler.schedule(() -> {
			synchronized (lock) {
				if (socket == null && !wanted.isEmpty()) {
					connect();
				}
			}
		}, delay, TimeUnit.SECONDS);
	}

	private void onDisconnect(String reason) {
		synchronized (lock) {
			socket = null;
			subscribed = new HashSet<>();
			log.warn("Finnhub WebSocket closed ({}); reconnecting in {}s", reason, backoffSeconds);
			scheduleReconnect();
		}
	}

	void handleMessage(String text) {
		JsonNode msg = json.readTree(text);
		String type = msg.path("type").asString("");
		switch (type) {
			case "trade" -> {
				for (JsonNode t : msg.path("data")) {
					String symbol = t.path("s").asString(null);
					if (symbol != null && t.has("p") && t.has("t")) {
						ticks.record(new PriceTick(symbol, new BigDecimal(t.path("p").asString()),
								Instant.ofEpochMilli(t.path("t").asLong())));
					}
				}
			}
			case "error" -> log.warn("Finnhub WebSocket error message: {}", msg.path("msg").asString(""));
			default -> {
				// "ping" and others: nothing to do
			}
		}
	}

	@Override
	public void destroy() {
		stopped = true;
		scheduler.shutdownNow();
		synchronized (lock) {
			if (socket != null) {
				socket.abort();
			}
		}
	}

	private final class Listener implements WebSocket.Listener {

		private final StringBuilder buffer = new StringBuilder();

		@Override
		public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
			buffer.append(data);
			if (last) {
				String text = buffer.toString();
				buffer.setLength(0);
				try {
					handleMessage(text);
				}
				catch (RuntimeException ex) {
					log.warn("Bad Finnhub WebSocket message: {}", ex.getClass().getSimpleName());
				}
			}
			ws.request(1);
			return null;
		}

		@Override
		public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
			onDisconnect("status " + statusCode);
			return null;
		}

		@Override
		public void onError(WebSocket ws, Throwable error) {
			onDisconnect(error.getClass().getSimpleName());
		}
	}
}
