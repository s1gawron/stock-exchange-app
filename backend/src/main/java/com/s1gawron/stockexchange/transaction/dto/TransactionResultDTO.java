package com.s1gawron.stockexchange.transaction.dto;

import com.s1gawron.stockexchange.transaction.model.Transaction;
import com.s1gawron.stockexchange.transaction.model.TransactionExecution;
import com.s1gawron.stockexchange.transaction.model.TransactionStatus;
import com.s1gawron.stockexchange.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @param balanceBlocked          money still blocked in the wallet for a purchase transaction, zero for a sell transaction
 * @param averageFillPrice        null until the first fill
 * @param transactionProcessDate  date when the transaction was filled or canceled, null while it is open
 */
public record TransactionResultDTO(Long transactionId, TransactionType transactionType, TransactionStatus transactionStatus, String stockTicker,
                                   String stockCurrency, BigDecimal stockPriceLimit, BigDecimal stockQuantity, BigDecimal filledQuantity,
                                   BigDecimal remainingQuantity, BigDecimal averageFillPrice, BigDecimal balanceBlocked,
                                   LocalDateTime transactionCreateDate, LocalDateTime transactionProcessDate,
                                   List<TransactionExecutionDTO> executions) {

    public static TransactionResultDTO from(final Transaction transaction, final List<TransactionExecution> executions) {
        return new TransactionResultDTO(transaction.getId(), transaction.getTransactionType(), transaction.getTransactionStatus(),
            transaction.getTransactionPosition().getStockTicker(), transaction.getTransactionPosition().getStockCurrency(),
            transaction.getTransactionPosition().getStockPriceLimit(), transaction.getTransactionPosition().getStockQuantity(),
            transaction.getFilledQuantity(), transaction.getRemainingQuantity(), transaction.getAverageFillPrice().orElse(null),
            transaction.getBalanceBlocked(), transaction.getTransactionCreateDate(), transaction.getTransactionProcessDate(),
            executions.stream().map(TransactionExecutionDTO::from).toList());
    }

}
