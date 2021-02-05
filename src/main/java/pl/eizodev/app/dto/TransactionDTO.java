package pl.eizodev.app.dto;

import lombok.Builder;
import pl.eizodev.app.entities.StockIndex;
import pl.eizodev.app.entities.Transaction;
import pl.eizodev.app.entities.TransactionType;

import java.math.BigDecimal;

@Builder
public class TransactionDTO {
    private final Long transactionId;
    private final Long userId;
    private final TransactionType transactionType;
    private final StockIndex stockIndex;
    private final String stockTicker;
    private final BigDecimal stockPrice;
    private final int stockQuantity;

    public static TransactionDTO of(final Transaction transaction) {
        return TransactionDTO.builder()
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUserId())
                .transactionType(transaction.getTransactionType())
                .stockIndex(transaction.getStockIndex())
                .stockTicker(transaction.getStockTicker())
                .stockPrice(transaction.getStockPrice())
                .stockQuantity(transaction.getStockQuantity())
                .build();
    }
}
