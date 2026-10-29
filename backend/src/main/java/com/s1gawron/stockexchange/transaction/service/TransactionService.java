package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockQuoteDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.validator.TransactionRequestDTOValidator;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private static final int CURRENCY_CODE_LENGTH = 3;

    private final TransactionExecutionService transactionExecutionService;

    private final StockDataProvider finnhubStockDataProvider;

    public TransactionService(final TransactionExecutionService transactionExecutionService, final StockDataProvider finnhubStockDataProvider) {
        this.transactionExecutionService = transactionExecutionService;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
    }

    public void validateCreateAndProcessTransaction(final TransactionRequestDTO transactionRequestDTO) {
        TransactionRequestDTOValidator.I.validate(transactionRequestDTO);

        final String stockTicker = transactionRequestDTO.stockTicker();
        final StockQuoteDTO stockQuote = finnhubStockDataProvider.getStockData(stockTicker).stockQuote();
        final BigDecimal currentPrice = getCurrentPrice(stockTicker, stockQuote);
        final String stockCurrency = getStockCurrency(stockTicker, stockQuote);

        transactionExecutionService.createAndProcessTransaction(transactionRequestDTO, currentPrice, stockCurrency);
    }

    private BigDecimal getCurrentPrice(final String stockTicker, final StockQuoteDTO stockQuote) {
        final BigDecimal currentPrice = stockQuote.currentPrice();

        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw TransactionProcessingException.createForUnavailablePrice(stockTicker);
        }

        return currentPrice;
    }

    private String getStockCurrency(final String stockTicker, final StockQuoteDTO stockQuote) {
        final String stockCurrency = stockQuote.stockCurrency();

        if (stockCurrency == null || stockCurrency.length() != CURRENCY_CODE_LENGTH) {
            throw TransactionProcessingException.createForUnavailableCurrency(stockTicker);
        }

        return stockCurrency;
    }

}
