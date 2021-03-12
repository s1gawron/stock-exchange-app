package pl.eizodev.app.services.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.entities.TransactionType;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class TransactionResult {
    private final TransactionType transactionType;
    private final String stockName;
    private final int stockQuantity;
    private final BigDecimal transactionCost;
    private final BigDecimal balanceAfterTransaction;
}
