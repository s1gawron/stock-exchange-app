package pl.eizodev.app.stock.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.eizodev.app.stock.dataprovider.StockDataProvider;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubCompanyProfileResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockQuoteResponseDTO;
import pl.eizodev.app.stock.dataprovider.dto.FinnhubStockSearchResponseDTO;
import pl.eizodev.app.stock.dto.StockDataDTO;
import pl.eizodev.app.stock.entity.StockCompanyDetails;

import java.util.Optional;

@Service
@AllArgsConstructor
public class StockDataProviderService {

    private final StockDataProvider stockDataProvider;

    private final StockCompanyDetailsService stockCompanyDetailsService;

    public FinnhubStockSearchResponseDTO findStock(final String ticker) {
        return stockDataProvider.findStock(ticker);
    }

    public StockDataDTO getStockData(final String ticker) {
        final Optional<StockCompanyDetails> stockCompanyDetails = stockCompanyDetailsService.getStockByTicker(ticker);

        if (stockCompanyDetails.isEmpty()) {
            final FinnhubCompanyProfileResponseDTO companyProfile = stockDataProvider.getCompanyProfile(ticker);
            final FinnhubStockQuoteResponseDTO stockQuote = stockDataProvider.getStockQuote(ticker);
            final StockCompanyDetails newStockCompanyDetails = StockCompanyDetails.createFrom(companyProfile, stockQuote);

            stockCompanyDetailsService.saveCompanyDetails(newStockCompanyDetails);

            return StockDataDTO.createFrom(newStockCompanyDetails);
        }

        final StockCompanyDetails updatedStockCompanyDetails = stockCompanyDetailsService.updateNumberOfInvokes(stockCompanyDetails.get().getId());
        return StockDataDTO.createFrom(updatedStockCompanyDetails);
    }

}
