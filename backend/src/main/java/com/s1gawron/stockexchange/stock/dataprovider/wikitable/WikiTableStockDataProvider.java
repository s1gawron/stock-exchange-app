package com.s1gawron.stockexchange.stock.dataprovider.wikitable;

import com.s1gawron.stockexchange.configuration.CacheConfiguration;
import com.s1gawron.stockexchange.stock.dataprovider.RequestLoggingInterceptor;
import com.s1gawron.stockexchange.stock.dataprovider.dto.IndexCompaniesDTO;
import com.s1gawron.stockexchange.stock.dataprovider.wikitable.exception.WikiTableConnectionFailedException;
import com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor.DjiCompanyDataExtractor;
import com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor.AbstractCompanyDataExtractor;
import com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor.Nasdaq100CompanyDataExtractor;
import com.s1gawron.stockexchange.stock.dataprovider.wikitable.extractor.Sp500CompanyDataExtractor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class WikiTableStockDataProvider {

    private static final String WIKI_TABLE_BASE_URL = "https://www.wikitable2json.com/api";

    @Cacheable(value = CacheConfiguration.INDEX_COMPANIES_CACHE)
    public IndexCompaniesDTO getIndexCompanies(final IndexSymbol symbol) {
        final List body = getRestClient().get()
            .uri(uriBuilder -> uriBuilder
                .path(symbol.getUrlPath())
                .queryParam("table", symbol.getTableNumber())
                .build()
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                throw WikiTableConnectionFailedException.create(response.getStatusCode().value());
            })
            .body(List.class);

        return getDataExtractor(symbol, body).extract();
    }

    private RestClient getRestClient() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        final Duration fiveSeconds = Duration.of(5, ChronoUnit.SECONDS);
        requestFactory.setConnectTimeout(fiveSeconds);
        requestFactory.setReadTimeout(fiveSeconds);

        return RestClient.builder()
            .requestInterceptor(new RequestLoggingInterceptor())
            .baseUrl(WIKI_TABLE_BASE_URL)
            .requestFactory(requestFactory)
            .build();
    }

    private AbstractCompanyDataExtractor getDataExtractor(final IndexSymbol symbol, final List<List<List<String>>> data) {
        return switch (symbol) {
            case DJI -> new DjiCompanyDataExtractor(data);
            case NASDAQ100 -> new Nasdaq100CompanyDataExtractor(data);
            case SP500 -> new Sp500CompanyDataExtractor(data);
        };
    }
}
