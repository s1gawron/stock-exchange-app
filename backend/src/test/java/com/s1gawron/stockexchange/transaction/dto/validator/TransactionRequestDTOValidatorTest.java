package com.s1gawron.stockexchange.transaction.dto.validator;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.PositionNotAllowedForPurchaseException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceFormatException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityFormatException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.TransactionRequestEmptyPropertiesException;
import com.s1gawron.stockexchange.transaction.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRequestDTOValidatorTest {

    @Test
    void shouldValidate() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"), new BigDecimal("10"));

        assertTrue(TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenTransactionTypeIsNull() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(null, "AAPL", new BigDecimal("280.00"), new BigDecimal("10"));

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockTickerIsNull() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, null, new BigDecimal("280.00"), new BigDecimal("10"));

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockTickerIsBlank() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "  ", new BigDecimal("280.00"), new BigDecimal("10"));

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldNormalizeStockTicker() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, " aapl ", new BigDecimal("280.00"), new BigDecimal("10"));

        assertEquals("AAPL", transactionRequestDTO.stockTicker());
    }

    @Test
    void shouldThrowExceptionWhenStockPriceIsNull() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", null, new BigDecimal("10"));

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockPriceIsLessThanZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("-100.00"), new BigDecimal("10"));

        assertThrows(StockPriceLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockPriceIsZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", BigDecimal.ZERO, new BigDecimal("10"));

        assertThrows(StockPriceLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsLessThanZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"), new BigDecimal("-9"));

        assertThrows(StockQuantityLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"), new BigDecimal("0"));

        assertThrows(StockQuantityLteZeroException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }


    @Test
    void shouldThrowExceptionWhenQuantityIsNull() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"), null);

        assertThrows(TransactionRequestEmptyPropertiesException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldValidateFractionalQuantity() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"),
            new BigDecimal("0.123456"));

        assertTrue(TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenQuantityHasMoreThanSixDecimalPlaces() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"),
            new BigDecimal("0.1234567"));

        assertThrows(StockQuantityFormatException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenQuantityHasTooManyIntegerDigits() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"),
            new BigDecimal("10000000000000"));

        assertThrows(StockQuantityFormatException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldValidateSellWithPositionId() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.SELL, "AAPL", new BigDecimal("280.00"),
            new BigDecimal("1"), 5L);

        assertTrue(TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenPurchaseHasPositionId() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00"),
            new BigDecimal("1"), 5L);

        assertThrows(PositionNotAllowedForPurchaseException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockPriceHasMoreThanFourDecimalPlaces() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.00001"), new BigDecimal("10"));

        assertThrows(StockPriceFormatException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldValidateStockPriceWithTrailingZeros() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("280.123400"), new BigDecimal("10"));

        assertTrue(TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldThrowExceptionWhenStockPriceHasTooManyIntegerDigits() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("1000000000000000"), new BigDecimal("10"));

        assertThrows(StockPriceFormatException.class, () -> TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

    @Test
    void shouldValidateStockPriceWithMaxIntegerDigits() {
        final TransactionRequestDTO transactionRequestDTO = new TransactionRequestDTO(TransactionType.PURCHASE, "AAPL", new BigDecimal("999999999999999.9999"), new BigDecimal("10"));

        assertTrue(TransactionRequestDTOValidator.I.validate(transactionRequestDTO));
    }

}
