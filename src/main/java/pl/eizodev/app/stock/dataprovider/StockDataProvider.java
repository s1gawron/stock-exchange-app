package pl.eizodev.app.stock.dataprovider;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dto.StockDataDTO;
import pl.eizodev.app.stock.exception.FinnhubConnectionFailedException;
import pl.eizodev.app.stock.exception.StockNotFoundException;
import pl.eizodev.app.stock.model.StockCompanyDetails;
import pl.eizodev.app.stock.repository.StockCompanyDetailsRepository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@AllArgsConstructor
public class StockDataProvider {

    private final StockCompanyDetailsRepository stockCompanyDetailsRepository;

    private final FinnhubConnectionFactory finnhubConnectionFactory;

    public FinnhubStockSearchResponseDTO findStock(final String query) {
        final ResponseEntity<FinnhubStockSearchResponseDTO> response = finnhubConnectionFactory.getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/search")
                .queryParam("q", query).build())
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubStockSearchResponseDTO.class)
            .block();

        if (response == null) {
            throw FinnhubConnectionFailedException.create();
        }

        if (response.getBody() == null || response.getBody().getCount() == 0) {
            throw StockNotFoundException.createFromQuery(query);
        }

        return response.getBody();
    }

    public StockDataDTO getStockData(final String ticker) {
        final Optional<StockCompanyDetails> stockCompanyDetailsOptional = stockCompanyDetailsRepository.findByTicker(ticker);

        if (stockCompanyDetailsOptional.isEmpty()) {
            final FinnhubCompanyProfileResponseDTO companyProfile = getCompanyProfile(ticker);
            final FinnhubStockQuoteResponseDTO stockQuote = getStockQuote(ticker);
            final StockCompanyDetails stockCompanyDetails = StockCompanyDetails.createFrom(companyProfile, stockQuote);

            stockCompanyDetailsRepository.save(stockCompanyDetails);

            return StockDataDTO.createFrom(stockCompanyDetails);
        }

        return StockDataDTO.createFrom(stockCompanyDetailsOptional.get());
    }

    private FinnhubCompanyProfileResponseDTO getCompanyProfile(final String ticker) {
        final ResponseEntity<FinnhubCompanyProfileResponseDTO> response = finnhubConnectionFactory.getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/stock/profile2")
                .queryParam("symbol", ticker).build())
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubCompanyProfileResponseDTO.class)
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
        final ResponseEntity<FinnhubStockQuoteResponseDTO> response = finnhubConnectionFactory.getWebClient().get()
            .uri(uriBuilder -> uriBuilder
                .path("/quote")
                .queryParam("symbol", ticker).build())
            .retrieve()
            .onStatus(HttpStatus::isError, clientResponse -> Mono.error(FinnhubConnectionFailedException.create(clientResponse.rawStatusCode())))
            .toEntity(FinnhubStockQuoteResponseDTO.class)
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
