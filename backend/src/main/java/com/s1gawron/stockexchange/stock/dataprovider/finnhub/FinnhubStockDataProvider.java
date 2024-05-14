package com.s1gawron.stockexchange.stock.dataprovider.finnhub;

import com.s1gawron.stockexchange.configuration.CacheConfiguration;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubCompanyProfileDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubStockQuoteDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.dto.FinnhubStockSearchDTO;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockDataDTO;
import com.s1gawron.stockexchange.stock.dataprovider.finnhub.exception.FinnhubConnectionFailedException;
import com.s1gawron.stockexchange.stock.dataprovider.exception.StockNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
public class FinnhubStockDataProvider {

    private static final String FINNHUB_HEADER_NAME = "X-Finnhub-Token";

    private final String finnhubToken;

    private final String baseUrl;

    public FinnhubStockDataProvider(@Value("${finnhub.token}") final String finnhubToken, @Value("${finnhub.baseUrl}") final String baseUrl) {
        this.finnhubToken = finnhubToken;
        this.baseUrl = baseUrl;
    }

    @Cacheable(value = CacheConfiguration.STOCK_SEARCH_CACHE)
    public FinnhubStockSearchDTO findStock(final String query) {
        final FinnhubStockSearchDTO stockSearch = getRestClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("q", query).build()
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                throw FinnhubConnectionFailedException.create(response.getStatusCode().value());
            })
            .body(FinnhubStockSearchDTO.class);

        if (stockSearch == null || stockSearch.count() == 0) {
            throw StockNotFoundException.createFromQuery(query);
        }

        return stockSearch;
    }

    @Cacheable(value = CacheConfiguration.STOCK_DATA_CACHE)
    public StockDataDTO getStockData(final String ticker) {
        final FinnhubCompanyProfileDTO companyProfile = getCompanyProfile(ticker);
        final FinnhubStockQuoteDTO stockQuote = getStockQuote(ticker);

        return StockDataDTO.createFrom(companyProfile, stockQuote);
    }

    private FinnhubCompanyProfileDTO getCompanyProfile(final String ticker) {
        final FinnhubCompanyProfileDTO companyProfile = getRestClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/stock/profile2")
                .queryParam("symbol", ticker).build()
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                throw FinnhubConnectionFailedException.create(response.getStatusCode().value());
            })
            .body(FinnhubCompanyProfileDTO.class);

        if (companyProfile == null || companyProfile.isEmpty()) {
            throw StockNotFoundException.createFromTicker(ticker);
        }

        return companyProfile;
    }

    private FinnhubStockQuoteDTO getStockQuote(final String ticker) {
        final FinnhubStockQuoteDTO stockQuote = getRestClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/quote")
                .queryParam("symbol", ticker).build()
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                throw FinnhubConnectionFailedException.create(response.getStatusCode().value());
            })
            .body(FinnhubStockQuoteDTO.class);

        if (stockQuote == null || stockQuote.isEmpty()) {
            throw StockNotFoundException.createFromTicker(ticker);
        }

        return stockQuote;
    }

    private RestClient getRestClient() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        final Duration fiveSeconds = Duration.of(5, ChronoUnit.SECONDS);
        requestFactory.setConnectTimeout(fiveSeconds);
        requestFactory.setReadTimeout(fiveSeconds);

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .defaultHeader(FINNHUB_HEADER_NAME, finnhubToken)
            .build();
    }
}
