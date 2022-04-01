package pl.eizodev.app.stock.dataprovider;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.StockDataDTO;
import pl.eizodev.app.stock.dataprovider.exception.FinnhubConnectionFailedException;
import pl.eizodev.app.stock.dataprovider.exception.StockNotFoundException;
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

    @Cacheable(cacheNames = "stockSearchCache")
    public FinnhubStockSearchResponseDTO findStock(final String query) {
        final ResponseEntity<FinnhubStockSearchResponseDTO> response = getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("q", query).build())
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubStockSearchResponseDTO.class)
            .onErrorResume(throwable -> Mono.error(FinnhubConnectionFailedException.create(throwable.getMessage())))
            .block();

        if (response == null) {
            throw FinnhubConnectionFailedException.create();
        }

        if (response.getBody() == null || response.getBody().getCount() == 0) {
            throw StockNotFoundException.createFromQuery(query);
        }

        return response.getBody();
    }

    @Cacheable(cacheNames = "stockDataCache")
    public StockDataDTO getStockData(final String ticker) {
        final FinnhubCompanyProfileResponseDTO companyProfile = getCompanyProfile(ticker);
        final FinnhubStockQuoteResponseDTO stockQuote = getStockQuote(ticker);

        return StockDataDTO.createFrom(companyProfile, stockQuote);
    }

    private FinnhubCompanyProfileResponseDTO getCompanyProfile(final String ticker) {
        final ResponseEntity<FinnhubCompanyProfileResponseDTO> response = getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/stock/profile2")
                .queryParam("symbol", ticker).build())
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubCompanyProfileResponseDTO.class)
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

    private FinnhubStockQuoteResponseDTO getStockQuote(final String ticker) {
        final ResponseEntity<FinnhubStockQuoteResponseDTO> response = getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/quote")
                .queryParam("symbol", ticker).build())
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubStockQuoteResponseDTO.class)
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
