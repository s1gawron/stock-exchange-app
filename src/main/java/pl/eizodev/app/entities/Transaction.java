package pl.eizodev.app.entities;

import lombok.*;
import pl.eizodev.app.dto.TransactionDTO;

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

    public Transaction(Long userId, TransactionType transactionType, StockIndex stockIndex, String stockTicker, BigDecimal stockPrice, int stockQuantity) {
        this.userId = userId;
        this.transactionType = transactionType;
        this.stockIndex = stockIndex;
        this.stockTicker = stockTicker;
        this.stockPrice = stockPrice;
        this.stockQuantity = stockQuantity;
    }

    public static Transaction of(final TransactionDTO transactionDTO) {
        return new Transaction(transactionDTO.getUserId(), transactionDTO.getTransactionType(),
                transactionDTO.getStockIndex(), transactionDTO.getStockTicker(), transactionDTO.getStockPrice(), transactionDTO.getStockQuantity());
    }
}