package com.s1gawron.stockexchange.transaction.matching.feed.finnhub;

import com.s1gawron.stockexchange.transaction.matching.feed.TradeBuffer;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Creates the Finnhub websocket trade feed. Set {@code finnhub.websocket.enabled=false} to use {@code NoOpTradeFeed} instead.
 */
@Configuration
@ConditionalOnProperty(name = "finnhub.websocket.enabled", havingValue = "true", matchIfMissing = true)
public class FinnhubWebSocketConfiguration {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public FinnhubWebSocketTradeFeed finnhubWebSocketTradeFeed(@Value("${finnhub.token}") final String finnhubToken,
        @Value("${finnhub.websocket.url:wss://ws.finnhub.io}") final String url,
        @Value("${finnhub.websocket.allowed-trade-conditions:1,7,8}") final Set<String> allowedTradeConditions,
        @Value("${finnhub.websocket.accept-trades-without-conditions:false}") final boolean acceptTradesWithoutConditions,
        @Value("${finnhub.websocket.max-reconnect-delay:PT1M}") final Duration maxReconnectDelay, final TradeBuffer tradeBuffer,
        final MeterRegistry meterRegistry) {
        final URI uri = URI.create(url + "?token=" + URLEncoder.encode(finnhubToken, StandardCharsets.UTF_8));
        final HttpClient httpClient = HttpClient.newHttpClient();
        final WebSocketConnector webSocketConnector = (webSocketUri, listener) -> httpClient.newWebSocketBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .buildAsync(webSocketUri, listener);
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "finnhub-websocket-reconnect");
            thread.setDaemon(true);
            return thread;
        });

        return new FinnhubWebSocketTradeFeed(uri, webSocketConnector, new FinnhubTradeFilter(allowedTradeConditions, acceptTradesWithoutConditions),
            tradeBuffer, ObjectMapperFactory.I.getMapper(), meterRegistry, scheduler, maxReconnectDelay);
    }

}
