package com.stocksim.realtime;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.stocksim.auth.AppUser;
import com.stocksim.marketdata.PriceTick;
import com.stocksim.marketdata.TradingSession;
import com.stocksim.marketdata.UsMarketCalendar;
import com.stocksim.trading.OrderUpdatedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Browser WebSocket at /ws. Client sends {@code {"type":"subscribe","symbols":["AAPL",...]}} (replaces its set).
 * Server sends {@code {"type":"price",...}} batched every 500 ms per symbol, and {@code {"type":"order","order":...}}
 * to the order's owner.
 */
@Component
public class PriceHub extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(PriceHub.class);
	private static final int MAX_SYMBOLS_PER_SESSION = 50;

	private record Client(WebSocketSession session, long userId, Set<String> symbols) {
	}

	private record PriceMessage(String type, String symbol, BigDecimal price, Instant timestamp,
			TradingSession session) {
	}

	private record OrderMessage(String type, Object order) {
	}

	private final JsonMapper json;
	private final UsMarketCalendar calendar;
	private final Map<String, Client> clients = new ConcurrentHashMap<>();
	private final Map<String, PriceTick> pending = new ConcurrentHashMap<>();

	PriceHub(JsonMapper json, UsMarketCalendar calendar) {
		this.json = json;
		this.calendar = calendar;
	}

	/** Symbols any connected browser wants to see, for the live feed's subscription list. */
	public Set<String> viewedSymbols() {
		Set<String> all = new LinkedHashSet<>();
		clients.values().forEach(c -> all.addAll(c.symbols()));
		return all;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		if (!(session.getPrincipal() instanceof Authentication auth) || !(auth.getPrincipal() instanceof AppUser user)) {
			close(session, CloseStatus.POLICY_VIOLATION);
			return;
		}
		var safe = new ConcurrentWebSocketSessionDecorator(session, 5_000, 512 * 1024);
		clients.put(session.getId(), new Client(safe, user.id(), ConcurrentHashMap.newKeySet()));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		Client client = clients.get(session.getId());
		if (client == null) {
			return;
		}
		JsonNode msg = json.readTree(message.getPayload());
		if (!"subscribe".equals(msg.path("type").asString(""))) {
			return;
		}
		Set<String> symbols = new HashSet<>();
		for (JsonNode s : msg.path("symbols")) {
			String symbol = s.asString("").trim().toUpperCase();
			if (symbol.matches("[A-Z0-9.\\-]{1,32}") && symbols.size() < MAX_SYMBOLS_PER_SESSION) {
				symbols.add(symbol);
			}
		}
		client.symbols().retainAll(symbols);
		client.symbols().addAll(symbols);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		clients.remove(session.getId());
	}

	@EventListener
	void onTick(PriceTick tick) {
		pending.merge(tick.symbol(), tick, (a, b) -> b.timestamp().isAfter(a.timestamp()) ? b : a);
	}

	@Scheduled(fixedDelay = 500)
	void flushPrices() {
		if (pending.isEmpty()) {
			return;
		}
		Map<String, PriceTick> batch = new HashMap<>();
		for (String symbol : new ArrayList<>(pending.keySet())) {
			PriceTick t = pending.remove(symbol);
			if (t != null) {
				batch.put(symbol, t);
			}
		}
		for (Client c : clients.values()) {
			for (String symbol : c.symbols()) {
				PriceTick t = batch.get(symbol);
				if (t != null) {
					send(c, new PriceMessage("price", t.symbol(), t.price(), t.timestamp(),
							calendar.sessionAt(t.timestamp())));
				}
			}
		}
	}

	@EventListener
	void onOrderUpdated(OrderUpdatedEvent event) {
		for (Client c : clients.values()) {
			if (c.userId() == event.userId()) {
				send(c, new OrderMessage("order", event.order()));
			}
		}
	}

	List<Long> connectedUserIds() {
		return clients.values().stream().map(Client::userId).toList();
	}

	private void send(Client c, Object payload) {
		try {
			c.session().sendMessage(new TextMessage(json.writeValueAsString(payload)));
		}
		catch (IOException | IllegalStateException ex) {
			log.debug("Dropping WebSocket client: {}", ex.getMessage());
			clients.remove(c.session().getId());
			close(c.session(), CloseStatus.SESSION_NOT_RELIABLE);
		}
	}

	private static void close(WebSocketSession session, CloseStatus status) {
		try {
			session.close(status);
		}
		catch (IOException ignored) {
			// already closed
		}
	}
}
