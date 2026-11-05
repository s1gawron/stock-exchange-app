package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.model.TransactionExecution;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionExecutionDTO(BigDecimal quantity, BigDecimal price, LocalDateTime executionDate) {

    public static TransactionExecutionDTO from(final TransactionExecution transactionExecution) {
        return new TransactionExecutionDTO(transactionExecution.getQuantity(), transactionExecution.getPrice(), transactionExecution.getExecutionDate());
    }

}
