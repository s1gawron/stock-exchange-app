package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.validator.TransactionRequestDTOValidator;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionExecutionService transactionExecutionService;

    private final StockDataProvider finnhubStockDataProvider;

    public TransactionService(final TransactionExecutionService transactionExecutionService, final StockDataProvider finnhubStockDataProvider) {
        this.transactionExecutionService = transactionExecutionService;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
    }

    public void validateCreateAndProcessTransaction(final TransactionRequestDTO transactionRequestDTO) {
        TransactionRequestDTOValidator.I.validate(transactionRequestDTO);

        final BigDecimal currentPrice = getCurrentPrice(transactionRequestDTO.stockTicker());
        transactionExecutionService.createAndProcessTransaction(transactionRequestDTO, currentPrice);
    }

    private BigDecimal getCurrentPrice(final String stockTicker) {
        final BigDecimal currentPrice = finnhubStockDataProvider.getStockData(stockTicker).stockQuote().currentPrice();

        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw TransactionProcessingException.createForUnavailablePrice(stockTicker);
        }

        return currentPrice;
    }

}
