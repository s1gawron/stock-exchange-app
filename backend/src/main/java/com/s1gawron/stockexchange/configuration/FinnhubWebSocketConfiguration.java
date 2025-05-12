package com.s1gawron.stockexchange.configuration;

import com.s1gawron.stockexchange.websocket.FinnhubWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Configuration
public class FinnhubWebSocketConfiguration {

    private final String finnhubWebSocketUrl;

    private final String finnhubToken;

    public FinnhubWebSocketConfiguration(@Value("${finnhub.webSocketUrl}") final String finnhubWebSocketUrl,
        @Value("${finnhub.token}") final String finnhubToken) {
        this.finnhubWebSocketUrl = finnhubWebSocketUrl;
        this.finnhubToken = finnhubToken;
    }

    @Bean
    public WebSocketConnectionManager webSocketConnectionManager(final FinnhubWebSocketHandler finnhubWebSocketHandler) {
        final String webSocketUrl = finnhubWebSocketUrl + finnhubToken;
        final WebSocketConnectionManager manager = new WebSocketConnectionManager(new StandardWebSocketClient(), finnhubWebSocketHandler, webSocketUrl);
        manager.setAutoStartup(true);

        return manager;
    }
}
