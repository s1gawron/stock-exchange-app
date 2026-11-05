package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import com.s1gawron.stockexchange.transaction.matching.Trade;
import com.s1gawron.stockexchange.transaction.matching.feed.TradeBuffer;
import com.s1gawron.stockexchange.transaction.matching.feed.TradeFeed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Receives real-time trades from the Finnhub websocket and publishes the normal trades to {@link TradeBuffer}.
 * <p>
 * Finnhub allows only one websocket connection per API key, so only one application instance can run this feed. The feed keeps the set of
 * subscribed tickers, sends it again after each reconnect and reconnects with exponential backoff when the connection is lost.
 */
public class FinnhubWebSocketTradeFeed implements TradeFeed, SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(FinnhubWebSocketTradeFeed.class);

    private static final Duration INITIAL_RECONNECT_DELAY = Duration.ofSeconds(1);

    private static final String TRADES_METRIC = "finnhub.websocket.trades";

    private final URI uri;

    private final WebSocketConnector webSocketConnector;

    private final FinnhubTradeFilter tradeFilter;

    private final TradeBuffer tradeBuffer;

    private final ObjectMapper objectMapper;

    private final ScheduledExecutorService scheduler;

    private final Duration maxReconnectDelay;

    private final Map<FinnhubTradeFilter.Result, Counter> tradeCounters = new EnumMap<>(FinnhubTradeFilter.Result.class);

    // All fields below are guarded by "this".
    private final Set<String> subscriptions = new HashSet<>();

    private WebSocket webSocket;

    private CompletableFuture<?> lastSend = CompletableFuture.completedFuture(null);

    private long connectionId;

    private int reconnectAttempts;

    private boolean running;

    public FinnhubWebSocketTradeFeed(final URI uri, final WebSocketConnector webSocketConnector, final FinnhubTradeFilter tradeFilter,
        final TradeBuffer tradeBuffer, final ObjectMapper objectMapper, final MeterRegistry meterRegistry, final ScheduledExecutorService scheduler,
        final Duration maxReconnectDelay) {
        this.uri = uri;
        this.webSocketConnector = webSocketConnector;
        this.tradeFilter = tradeFilter;
        this.tradeBuffer = tradeBuffer;
        this.objectMapper = objectMapper;
        this.scheduler = scheduler;
        this.maxReconnectDelay = maxReconnectDelay;

        for (final FinnhubTradeFilter.Result result : FinnhubTradeFilter.Result.values()) {
            tradeCounters.put(result, meterRegistry.counter(TRADES_METRIC, "result", result.name().toLowerCase(Locale.ROOT)));
        }
    }

    @Override
    public synchronized void start() {
        running = true;
        connect();
    }

    @Override
    public synchronized void stop() {
        running = false;
        connectionId++;

        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Application shutdown");
            webSocket = null;
        }

        scheduler.shutdownNow();
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void subscribe(final String ticker) {
        if (subscriptions.add(ticker) && webSocket != null) {
            send("subscribe", ticker);
        }
    }

    @Override
    public synchronized void unsubscribe(final String ticker) {
        if (subscriptions.remove(ticker) && webSocket != null) {
            send("unsubscribe", ticker);
        }
    }

    public synchronized boolean isConnected() {
        return webSocket != null;
    }

    public synchronized Set<String> getSubscriptions() {
        return Set.copyOf(subscriptions);
    }

    void handleMessage(final String message) {
        final FinnhubWebSocketMessageDTO webSocketMessage;

        try {
            webSocketMessage = objectMapper.readValue(message, FinnhubWebSocketMessageDTO.class);
        } catch (final JacksonException e) {
            log.warn("Could not read Finnhub websocket message: {}", e.getOriginalMessage());
            return;
        }

        if (FinnhubWebSocketMessageDTO.TRADE_TYPE.equals(webSocketMessage.type())) {
            handleTrades(webSocketMessage);
        } else if (FinnhubWebSocketMessageDTO.ERROR_TYPE.equals(webSocketMessage.type())) {
            log.warn("Finnhub websocket returned an error: {}", webSocketMessage.msg());
        } else if (!FinnhubWebSocketMessageDTO.PING_TYPE.equals(webSocketMessage.type())) {
            log.debug("Finnhub websocket message of type: {} ignored", webSocketMessage.type());
        }
    }

    private void handleTrades(final FinnhubWebSocketMessageDTO webSocketMessage) {
        if (webSocketMessage.data() == null) {
            return;
        }

        for (final FinnhubTradeDTO trade : webSocketMessage.data()) {
            final FinnhubTradeFilter.Result result = tradeFilter.check(trade);
            tradeCounters.get(result).increment();

            if (result == FinnhubTradeFilter.Result.ACCEPTED) {
                tradeBuffer.publish(new Trade(trade.symbol(), trade.price(), trade.volume(), Instant.ofEpochMilli(trade.timestamp())));
            }
        }
    }

    private synchronized void connect() {
        if (!running) {
            return;
        }

        final long currentConnectionId = ++connectionId;
        log.info("Connecting to Finnhub websocket");

        // The URI contains the API token, so it is never logged.
        webSocketConnector.connect(uri, new Listener(currentConnectionId)).whenComplete((connectedWebSocket, error) -> {
            if (error != null) {
                log.warn("Could not connect to Finnhub websocket: {}", error.getMessage());
                onDisconnected(currentConnectionId);
            } else {
                onConnected(currentConnectionId, connectedWebSocket);
            }
        });
    }

    private synchronized void onConnected(final long currentConnectionId, final WebSocket connectedWebSocket) {
        if (currentConnectionId != connectionId || !running) {
            connectedWebSocket.abort();
            return;
        }

        webSocket = connectedWebSocket;
        lastSend = CompletableFuture.completedFuture(null);
        reconnectAttempts = 0;

        log.info("Connected to Finnhub websocket, subscribing to {} tickers", subscriptions.size());
        subscriptions.forEach(ticker -> send("subscribe", ticker));
    }

    private synchronized void onDisconnected(final long currentConnectionId) {
        // Callbacks of an old connection are ignored, so one lost connection causes only one reconnect.
        if (currentConnectionId != connectionId) {
            return;
        }

        webSocket = null;

        if (!running) {
            return;
        }

        final long delayMillis = getReconnectDelay().toMillis();
        reconnectAttempts++;
        log.info("Reconnecting to Finnhub websocket in {} ms", delayMillis);
        scheduler.schedule(this::connect, delayMillis, TimeUnit.MILLISECONDS);
    }

    private Duration getReconnectDelay() {
        final int exponent = Math.min(reconnectAttempts, 16);
        final Duration delay = INITIAL_RECONNECT_DELAY.multipliedBy(1L << exponent);

        return delay.compareTo(maxReconnectDelay) > 0 ? maxReconnectDelay : delay;
    }

    /**
     * A websocket allows only one send at a time, so each send waits for the previous one.
     */
    private void send(final String type, final String ticker) {
        final WebSocket currentWebSocket = webSocket;
        final String message = objectMapper.writeValueAsString(Map.of("type", type, "symbol", ticker));

        lastSend = lastSend.handle((result, error) -> null)
            .thenCompose(ignored -> currentWebSocket.sendText(message, true))
            .whenComplete((result, error) -> {
                if (error != null) {
                    log.warn("Could not send {} for {} to Finnhub websocket: {}", type, ticker, error.getMessage());
                }
            });
    }

    private final class Listener implements WebSocket.Listener {

        private final long listenerConnectionId;

        private final StringBuilder messageBuilder = new StringBuilder();

        private Listener(final long listenerConnectionId) {
            this.listenerConnectionId = listenerConnectionId;
        }

        @Override
        public void onOpen(final WebSocket openedWebSocket) {
            openedWebSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(final WebSocket currentWebSocket, final CharSequence data, final boolean last) {
            messageBuilder.append(data);

            if (last) {
                final String message = messageBuilder.toString();
                messageBuilder.setLength(0);
                handleMessage(message);
            }

            currentWebSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(final WebSocket closedWebSocket, final int statusCode, final String reason) {
            log.warn("Finnhub websocket closed with status: {}, reason: {}", statusCode, reason);
            onDisconnected(listenerConnectionId);
            return null;
        }

        @Override
        public void onError(final WebSocket failedWebSocket, final Throwable error) {
            log.warn("Finnhub websocket error: {}", error.getMessage());
            onDisconnected(listenerConnectionId);
        }
    }
}
