package pl.eizodev.app.stock.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.eizodev.app.stock.entity.StockCompanyDetails;
import pl.eizodev.app.stock.repository.StockCompanyDetailsRepository;
import pl.eizodev.app.stock.service.exception.StockCompanyDetailsNotFoundException;

import java.util.Optional;

@Service
@AllArgsConstructor
public class StockCompanyDetailsService {

    private final StockCompanyDetailsRepository stockCompanyDetailsRepository;

    @Transactional(readOnly = true)
    public Optional<StockCompanyDetails> getStockByTicker(final String ticker) {
        return stockCompanyDetailsRepository.findByTicker(ticker);
    }

    @Transactional
    public void saveCompanyDetails(final StockCompanyDetails stockCompanyDetails) {
        stockCompanyDetailsRepository.save(stockCompanyDetails);
    }

    @Transactional
    public StockCompanyDetails updateNumberOfInvokes(final long id) {
        final StockCompanyDetails stockCompanyDetails = stockCompanyDetailsRepository.findById(id)
            .orElseThrow(() -> StockCompanyDetailsNotFoundException.create(id));

        stockCompanyDetails.incrementNumberOfInvokes();
        stockCompanyDetailsRepository.save(stockCompanyDetails);

        return stockCompanyDetails;
    }

}
