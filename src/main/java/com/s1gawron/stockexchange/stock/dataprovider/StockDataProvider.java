package com.s1gawron.stockexchange.stock.dataprovider;

import com.s1gawron.stockexchange.configuration.CacheConfiguration;
import com.s1gawron.stockexchange.stock.dataprovider.dto.FinnhubCompanyProfileDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.FinnhubStockQuoteDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.FinnhubStockSearchDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.exception.FinnhubConnectionFailedException;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class StockDataProvider {

    private final String finnhubToken;

    private final String baseUrl;

    public StockDataProvider(@Value("${finnhub.token}") final String finnhubToken, @Value("${finnhub.baseUrl}") final String baseUrl) {
        this.finnhubToken = finnhubToken;
        this.baseUrl = baseUrl;
    }

    @Cacheable(value = CacheConfiguration.STOCK_SEARCH_CACHE)
    public FinnhubStockSearchDTO findStock(final String query) {
        final ResponseEntity<FinnhubStockSearchDTO> response = getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("q", query).build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubStockSearchDTO.class)
            .onErrorResume(throwable -> Mono.error(FinnhubConnectionFailedException.create(throwable.getMessage())))
            .block();

        if (response == null) {
            throw FinnhubConnectionFailedException.create();
        }

        if (response.getBody() == null || response.getBody().count() == 0) {
            throw StockNotFoundException.createFromQuery(query);
        }

        return response.getBody();
    }

    @Cacheable(value = CacheConfiguration.STOCK_DATA_CACHE)
    public StockDataDTO getStockData(final String ticker) {
        final FinnhubCompanyProfileDTO companyProfile = getCompanyProfile(ticker);
        final FinnhubStockQuoteDTO stockQuote = getStockQuote(ticker);

        return StockDataDTO.createFrom(companyProfile, stockQuote);
    }

    private FinnhubCompanyProfileDTO getCompanyProfile(final String ticker) {
        final ResponseEntity<FinnhubCompanyProfileDTO> response = getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/stock/profile2")
                .queryParam("symbol", ticker).build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubCompanyProfileDTO.class)
            .onErrorResume(throwable -> Mono.error(FinnhubConnectionFailedException.create(throwable.getMessage())))
            .block();

        if (response == null) {
            throw FinnhubConnectionFailedException.create();
        }

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw StockNotFoundException.createFromTicker(ticker);
        }

        return response.getBody();
    }

    private FinnhubStockQuoteDTO getStockQuote(final String ticker) {
        final ResponseEntity<FinnhubStockQuoteDTO> response = getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/quote")
                .queryParam("symbol", ticker).build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubStockQuoteDTO.class)
            .onErrorResume(throwable -> Mono.error(FinnhubConnectionFailedException.create(throwable.getMessage())))
            .block();

        if (response == null) {
            throw FinnhubConnectionFailedException.create();
        }

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw StockNotFoundException.createFromTicker(ticker);
        }

        return response.getBody();
    }

    private WebClient getWebClient() {
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
