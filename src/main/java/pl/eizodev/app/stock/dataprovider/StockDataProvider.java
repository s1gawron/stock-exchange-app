package pl.eizodev.app.stock.dataprovider;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dataprovider.exception.FinnhubConnectionFailedException;
import pl.eizodev.app.stock.dataprovider.exception.StockNotFoundException;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class StockDataProvider {

    private final FinnhubConnectionFactory finnhubConnectionFactory;

    public FinnhubStockSearchResponseDTO findStock(final String query) {
        final ResponseEntity<FinnhubStockSearchResponseDTO> response = finnhubConnectionFactory.getWebClient().get()
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

    public FinnhubCompanyProfileResponseDTO getCompanyProfile(final String ticker) {
        final ResponseEntity<FinnhubCompanyProfileResponseDTO> response = finnhubConnectionFactory.getWebClient().get()
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

    public FinnhubStockQuoteResponseDTO getStockQuote(final String ticker) {
        final ResponseEntity<FinnhubStockQuoteResponseDTO> response = finnhubConnectionFactory.getWebClient().get()
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
}
