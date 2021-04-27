package pl.eizodev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.eizodev.app.entity.StockIndex;
import pl.eizodev.app.entity.TransactionType;

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
