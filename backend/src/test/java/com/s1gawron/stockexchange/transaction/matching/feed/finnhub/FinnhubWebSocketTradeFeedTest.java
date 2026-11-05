package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import com.s1gawron.stockexchange.transaction.matching.Trade;
import com.s1gawron.stockexchange.transaction.matching.feed.TradeBuffer;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FinnhubWebSocketTradeFeedTest {

    private static final URI URI_WITH_TOKEN = URI.create("wss://ws.finnhub.io?token=secret");

    private final List<CompletableFuture<WebSocket>> connections = new ArrayList<>();

    private final List<WebSocket.Listener> listeners = new ArrayList<>();

    private TradeBuffer tradeBuffer;

    private SimpleMeterRegistry meterRegistry;

    private ScheduledExecutorService schedulerMock;

    private FinnhubWebSocketTradeFeed underTest;

    @BeforeEach
    void setUp() {
        tradeBuffer = new TradeBuffer(100);
        meterRegistry = new SimpleMeterRegistry();
        schedulerMock = Mockito.mock(ScheduledExecutorService.class);

        final WebSocketConnector connector = (uri, listener) -> {
            assertEquals(URI_WITH_TOKEN, uri);
            final CompletableFuture<WebSocket> connection = new CompletableFuture<>();
            connections.add(connection);
            listeners.add(listener);
            return connection;
        };

        underTest = new FinnhubWebSocketTradeFeed(URI_WITH_TOKEN, connector, new FinnhubTradeFilter(Set.of("1", "7", "8"), false), tradeBuffer,
            ObjectMapperFactory.I.getMapper(), meterRegistry, schedulerMock, Duration.ofSeconds(4));
    }

    @Test
    void shouldSubscribeTickersAddedBeforeConnectionAfterConnecting() {
        underTest.subscribe("AAPL");
        underTest.start();
        final FakeWebSocket webSocket = completeConnection(0);

        assertTrue(underTest.isConnected());
        assertEquals(List.of("{\"type\":\"subscribe\",\"symbol\":\"AAPL\"}"), webSocket.sentMessagesInJsonOrder());
    }

    @Test
    void shouldSendSubscribeAndUnsubscribeWhileConnected() {
        underTest.start();
        final FakeWebSocket webSocket = completeConnection(0);

        underTest.subscribe("AAPL");
        underTest.subscribe("AAPL");
        underTest.unsubscribe("AAPL");
        underTest.unsubscribe("MSFT");

        assertEquals(List.of("{\"type\":\"subscribe\",\"symbol\":\"AAPL\"}", "{\"type\":\"unsubscribe\",\"symbol\":\"AAPL\"}"),
            webSocket.sentMessagesInJsonOrder());
        assertTrue(underTest.getSubscriptions().isEmpty());
    }

    @Test
    void shouldReconnectWithBackoffAndSubscribeAgainAfterConnectionIsClosed() {
        underTest.subscribe("AAPL");
        underTest.start();
        completeConnection(0);

        listeners.get(0).onClose(Mockito.mock(WebSocket.class), 1006, "lost");

        assertFalse(underTest.isConnected());
        runScheduledReconnect(1000);
        assertEquals(2, connections.size());

        final FakeWebSocket newWebSocket = completeConnection(1);
        assertEquals(List.of("{\"type\":\"subscribe\",\"symbol\":\"AAPL\"}"), newWebSocket.sentMessagesInJsonOrder());
    }

    @Test
    void shouldIncreaseReconnectDelayUpToMaximumWhenConnectionFails() {
        underTest.start();

        connections.get(0).completeExceptionally(new IllegalStateException("refused"));
        runScheduledReconnect(1000);
        connections.get(1).completeExceptionally(new IllegalStateException("refused"));
        runScheduledReconnect(2000);
        connections.get(2).completeExceptionally(new IllegalStateException("refused"));
        runScheduledReconnect(4000);
        connections.get(3).completeExceptionally(new IllegalStateException("refused"));
        runScheduledReconnect(4000);
    }

    @Test
    void shouldIgnoreCloseOfOldConnection() {
        underTest.start();
        completeConnection(0);
        listeners.get(0).onError(Mockito.mock(WebSocket.class), new IllegalStateException("broken"));
        runScheduledReconnect(1000);
        completeConnection(1);

        listeners.get(0).onClose(Mockito.mock(WebSocket.class), 1000, "late close of old connection");

        assertTrue(underTest.isConnected());
        Mockito.verify(schedulerMock, Mockito.never()).schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));
    }

    @Test
    void shouldNotReconnectAfterStop() {
        underTest.start();
        final FakeWebSocket webSocket = completeConnection(0);

        underTest.stop();
        listeners.get(0).onClose(webSocket, 1000, "closed");

        assertTrue(webSocket.closed);
        assertFalse(underTest.isRunning());
        Mockito.verify(schedulerMock, Mockito.never()).schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.any(TimeUnit.class));
    }

    @Test
    void shouldPublishOnlyNormalTradesFromTradeMessage() {
        underTest.handleMessage("""
            {"type": "trade", "data": [
              {"s": "AAPL", "p": 187.42, "t": 1762354800000, "v": 100, "c": ["1"]},
              {"s": "AAPL", "p": 187.40, "t": 1762354800001, "v": 200, "c": ["1", "8"]},
              {"s": "AAPL", "p": 187.30, "t": 1762354800002, "v": 5, "c": ["1", "12"]},
              {"s": "AAPL", "p": 187.50, "t": 1762354800003, "v": 0, "c": ["1"]},
              {"s": "AAPL", "p": 187.60, "t": 1762354800004, "v": 50}
            ]}
            """);

        final List<Trade> trades = tradeBuffer.drain();
        assertEquals(2, trades.size());
        assertEquals("AAPL", trades.get(0).ticker());
        assertEquals(0, new BigDecimal("187.42").compareTo(trades.get(0).price()));
        assertEquals(0, new BigDecimal("100").compareTo(trades.get(0).volume()));
        assertEquals(Instant.ofEpochMilli(1762354800000L), trades.get(0).timestamp());

        assertEquals(2, countTrades("accepted"));
        assertEquals(1, countTrades("special_condition"));
        assertEquals(1, countTrades("no_volume"));
        assertEquals(1, countTrades("no_conditions"));
    }

    @Test
    void shouldJoinMessageSentInSeveralParts() {
        underTest.start();
        final FakeWebSocket webSocket = completeConnection(0);

        listeners.get(0).onText(webSocket, "{\"type\": \"trade\", \"data\": [{\"s\": \"AAPL\", ", false);
        listeners.get(0).onText(webSocket, "\"p\": 187.42, \"t\": 1762354800000, \"v\": 100, \"c\": [\"1\"]}]}", true);

        assertEquals(1, tradeBuffer.drain().size());
        // One message requested when the connection opened and one after each part.
        assertEquals(3, webSocket.requested);
    }

    @Test
    void shouldIgnorePingErrorAndInvalidMessages() {
        underTest.handleMessage("{\"type\": \"ping\"}");
        underTest.handleMessage("{\"type\": \"error\", \"msg\": \"Subscribing to too many symbols\"}");
        underTest.handleMessage("not json");
        underTest.handleMessage("{\"type\": \"trade\"}");

        assertTrue(tradeBuffer.drain().isEmpty());
    }

    private FakeWebSocket completeConnection(final int index) {
        final FakeWebSocket webSocket = new FakeWebSocket();
        listeners.get(index).onOpen(webSocket);
        connections.get(index).complete(webSocket);

        return webSocket;
    }

    private void runScheduledReconnect(final long expectedDelayMillis) {
        final ArgumentCaptor<Runnable> reconnect = ArgumentCaptor.forClass(Runnable.class);
        Mockito.verify(schedulerMock).schedule(reconnect.capture(), Mockito.eq(expectedDelayMillis), Mockito.eq(TimeUnit.MILLISECONDS));
        Mockito.clearInvocations(schedulerMock);
        reconnect.getValue().run();
    }

    private double countTrades(final String result) {
        return meterRegistry.counter("finnhub.websocket.trades", "result", result).count();
    }

    private static final class FakeWebSocket implements WebSocket {

        private final List<String> sentMessages = new ArrayList<>();

        private int requested;

        private boolean closed;

        private List<String> sentMessagesInJsonOrder() {
            // Map.of does not keep key order, so the messages are compared with "type" first.
            return sentMessages.stream()
                .map(message -> ObjectMapperFactory.I.getMapper().readTree(message))
                .map(json -> "{\"type\":\"" + json.get("type").asString() + "\",\"symbol\":\"" + json.get("symbol").asString() + "\"}")
                .toList();
        }

        @Override
        public CompletableFuture<WebSocket> sendText(final CharSequence data, final boolean last) {
            sentMessages.add(data.toString());
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendBinary(final ByteBuffer data, final boolean last) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendPing(final ByteBuffer message) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendPong(final ByteBuffer message) {
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public CompletableFuture<WebSocket> sendClose(final int statusCode, final String reason) {
            closed = true;
            return CompletableFuture.completedFuture(this);
        }

        @Override
        public void request(final long n) {
            requested += (int) n;
        }

        @Override
        public String getSubprotocol() {
            return "";
        }

        @Override
        public boolean isOutputClosed() {
            return closed;
        }

        @Override
        public boolean isInputClosed() {
            return closed;
        }

        @Override
        public void abort() {
            closed = true;
        }
    }
}
