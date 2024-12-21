package com.s1gawron.stockexchange.transaction.dto.validator;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import com.s1gawron.stockexchange.transaction.exception.StockPriceLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.StockQuantityLteZeroException;
import com.s1gawron.stockexchange.transaction.exception.TransactionRequestEmptyPropertiesException;

import java.math.BigDecimal;

public enum TransactionRequestDTOValidator {

    I;

    public boolean validate(final TransactionRequestDTO transactionRequestDTO) {
        if (transactionRequestDTO.type() == null) {
            throw TransactionRequestEmptyPropertiesException.createForTransactionType();
        }

        if (transactionRequestDTO.stockTicker() == null) {
            throw TransactionRequestEmptyPropertiesException.createForStockTicker();
        }

        if (transactionRequestDTO.price() == null) {
            throw TransactionRequestEmptyPropertiesException.createForPrice();
        }

        if (transactionRequestDTO.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw StockPriceLteZeroException.create();
        }

        if (transactionRequestDTO.quantity() <= 0) {
            throw StockQuantityLteZeroException.create();
        }

        return true;
    }

}
