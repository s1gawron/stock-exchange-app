package pl.eizodev.app.transaction;

import lombok.*;
import pl.eizodev.app.stock.StockIndex;
import pl.eizodev.app.transaction.dto.TransactionDTO;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "stock_index")
    private StockIndex stockIndex;

    @Column(name = "ticker")
    private String stockTicker;

    @Column(name = "price")
    private BigDecimal stockPrice;

    @Column(name = "quantity")
    private int stockQuantity;

    public Transaction(final TransactionType transactionType, final StockIndex stockIndex, final String stockTicker, final BigDecimal stockPrice, final int stockQuantity) {
        this.transactionType = transactionType;
        this.stockIndex = stockIndex;
        this.stockTicker = stockTicker;
        this.stockPrice = stockPrice;
        this.stockQuantity = stockQuantity;
    }

    public static Transaction of(final TransactionDTO transactionDTO) {
        return new Transaction(transactionDTO.getTransactionType(),
                transactionDTO.getStockIndex(), transactionDTO.getStockTicker(), transactionDTO.getStockPrice(), transactionDTO.getStockQuantity());
    }
}