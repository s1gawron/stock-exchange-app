package com.s1gawron.stockexchange.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.websocket.dto.FinnhubSubscriptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;

@Profile("websockets")
@Component
public class FinnhubWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FinnhubWebSocketHandler.class);

    private final ObjectMapper objectMapper;

    private WebSocketSession currentSession;

    public FinnhubWebSocketHandler(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) {
        this.currentSession = session;
        log.info("Connected to Finnhub WebSocket");
    }

    @Override
    public void handleMessage(final WebSocketSession session, final WebSocketMessage<?> message) {
        log.info("Received message: {}", message);
    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) {
        log.error("Transport error: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus closeStatus) {
        log.info("Disconnected from Finnhub WebSocket with status: {}", closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void subscribe(final String symbol) {
        try {
            final FinnhubSubscriptionMessage finnhubSubscriptionMessage = FinnhubSubscriptionMessage.subscribe(symbol);
            final String jsonMessage = objectMapper.writeValueAsString(finnhubSubscriptionMessage);
            currentSession.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("Error subscribing to symbol {}: {}", symbol, e.getMessage());
        }
    }
}
