package com.s1gawron.stockexchange.transaction.dto.validator;

import com.s1gawron.stockexchange.shared.MoneyRounding;
import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.StockPriceFormatException;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.TransactionRequestEmptyPropertiesException;

import java.math.BigDecimal;

public enum TransactionRequestDTOValidator {

    I;

    private static final int MAX_PRICE_INTEGER_DIGITS = 15;

    public boolean validate(final TransactionRequestDTO transactionRequestDTO) {
        if (transactionRequestDTO.type() == null) {
            throw TransactionRequestEmptyPropertiesException.createForTransactionType();
        }

        if (transactionRequestDTO.stockTicker() == null || transactionRequestDTO.stockTicker().isEmpty()) {
            throw TransactionRequestEmptyPropertiesException.createForStockTicker();
        }

        if (transactionRequestDTO.price() == null) {
            throw TransactionRequestEmptyPropertiesException.createForPrice();
        }

        if (transactionRequestDTO.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw StockPriceLteZeroException.create();
        }

        final BigDecimal price = transactionRequestDTO.price().stripTrailingZeros();

        if (price.scale() > MoneyRounding.MONEY_SCALE) {
            throw StockPriceFormatException.createForDecimalPlaces(MoneyRounding.MONEY_SCALE);
        }

        if (price.precision() - price.scale() > MAX_PRICE_INTEGER_DIGITS) {
            throw StockPriceFormatException.createForIntegerDigits(MAX_PRICE_INTEGER_DIGITS);
        }

        if (transactionRequestDTO.quantity() <= 0) {
            throw StockQuantityLteZeroException.create();
        }

        return true;
    }

}
