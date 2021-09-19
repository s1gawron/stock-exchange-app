package pl.eizodev.app.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.stock.StockIndex;
import pl.eizodev.app.transaction.TransactionType;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class TransactionDTO {
    private final TransactionType transactionType;
    private final StockIndex stockIndex;
    private final String stockTicker;
    private final BigDecimal stockPrice;
    private final int stockQuantity;
}
