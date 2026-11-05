package com.s1gawron.stockexchange.transaction.service;

import com.s1gawron.stockexchange.stock.dataprovider.StockDataProvider;
import com.s1gawron.stockexchange.stock.dataprovider.dto.StockQuoteDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionModifyRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.dto.TransactionResultDTO;
import com.s1gawron.stockexchange.transaction.dto.validator.TransactionRequestDTOValidator;
import com.s1gawron.stockexchange.transaction.exception.TransactionProcessingException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private static final int CURRENCY_CODE_LENGTH = 3;

    private final TransactionExecutionService transactionExecutionService;

    private final StockDataProvider finnhubStockDataProvider;

    public TransactionService(final TransactionExecutionService transactionExecutionService, final StockDataProvider finnhubStockDataProvider) {
        this.transactionExecutionService = transactionExecutionService;
        this.finnhubStockDataProvider = finnhubStockDataProvider;
    }

    public TransactionResultDTO validateAndCreateTransaction(final TransactionRequestDTO transactionRequestDTO) {
        TransactionRequestDTOValidator.I.validate(transactionRequestDTO);

        final String stockTicker = transactionRequestDTO.stockTicker();
        final StockQuoteDTO stockQuote = finnhubStockDataProvider.getStockData(stockTicker).stockQuote();
        assertCurrentPriceAvailable(stockTicker, stockQuote);
        final String stockCurrency = getStockCurrency(stockTicker, stockQuote);

        return transactionExecutionService.createTransaction(transactionRequestDTO, stockCurrency);
    }

    /**
     * The type, ticker and sell position of the transaction do not change, only the price limit and the quantity. The new values are validated
     * with the same rules as a new transaction.
     */
    public TransactionResultDTO validateAndModifyTransaction(final long transactionId, final TransactionModifyRequestDTO transactionModifyRequestDTO) {
        TransactionRequestDTOValidator.I.validatePriceAndQuantity(transactionModifyRequestDTO.price(), transactionModifyRequestDTO.quantity());

        return transactionExecutionService.modifyTransaction(transactionId, transactionModifyRequestDTO.price(), transactionModifyRequestDTO.quantity());
    }

    public TransactionResultDTO cancelTransaction(final long transactionId) {
        return transactionExecutionService.cancelTransaction(transactionId);
    }

    public List<TransactionResultDTO> getTransactions() {
        return transactionExecutionService.getTransactions();
    }

    public TransactionResultDTO getTransaction(final long transactionId) {
        return transactionExecutionService.getTransaction(transactionId);
    }

    /**
     * Finnhub returns a zero price for an unknown ticker, so a missing price means that the stock cannot be traded.
     */
    private void assertCurrentPriceAvailable(final String stockTicker, final StockQuoteDTO stockQuote) {
        final BigDecimal currentPrice = stockQuote.currentPrice();

        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw TransactionProcessingException.createForUnavailablePrice(stockTicker);
        }
    }

    private String getStockCurrency(final String stockTicker, final StockQuoteDTO stockQuote) {
        final String stockCurrency = stockQuote.stockCurrency();

        if (stockCurrency == null || stockCurrency.length() != CURRENCY_CODE_LENGTH) {
            throw TransactionProcessingException.createForUnavailableCurrency(stockTicker);
        }

        return stockCurrency;
    }

}
