package pl.eizodev.app.stock.dataprovider;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class FinnhubConnectionFactory {

    private final String finnhubToken;

    private final String baseUrl;

    public FinnhubConnectionFactory(@Value("${finnhub.token}") final String finnhubToken, @Value("${finnhub.baseUrl}") final String baseUrl) {
        this.finnhubToken = finnhubToken;
        this.baseUrl = baseUrl;
    }

    public WebClient getWebClient() {
        final HttpClient httpClient = HttpClient.create()
            .resolver(DefaultAddressResolverGroup.INSTANCE)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofMillis(5000))
            .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(baseUrl)
            .defaultHeaders(headers -> headers.putAll(getHeaders()))
            .build();
    }

    private MultiValueMap<String, String> getHeaders() {
        final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        map.add("X-Finnhub-Token", finnhubToken);
        return map;
    }
}
