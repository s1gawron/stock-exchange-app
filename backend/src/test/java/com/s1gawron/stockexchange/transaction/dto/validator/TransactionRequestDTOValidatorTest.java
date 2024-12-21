package com.s1gawron.stockexchange.transaction.dto.validator;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.TransactionRequestEmptyPropertiesException;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRequestDTOValidatorTest {

    @Test
    void shouldValidate() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"), 10);

        assertTrue(TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenTransactionTypeIsNull() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(null, "AAPL", new BigDecimal("280.00"), 10);

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockTickerIsNull() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, null, new BigDecimal("280.00"), 10);

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockPriceIsNull() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", null, 10);

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockPriceIsLessThanZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("-100.00"), 10);

        assertThrows(StockPriceLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockPriceIsZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", BigDecimal.ZERO, 10);

        assertThrows(StockPriceLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsLessThanZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"), -9);

        assertThrows(StockQuantityLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"), 0);

        assertThrows(StockQuantityLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

}