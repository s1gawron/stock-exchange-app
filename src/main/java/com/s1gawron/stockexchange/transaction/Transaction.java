package com.s1gawron.stockexchange.transaction;

import com.s1gawron.stockexchange.transaction.dto.TransactionDTO;

import javax.persistence.*;
import java.math.BigDecimal;

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

    @Column(name = "ticker")
    private String stockTicker;

    @Column(name = "price")
    private BigDecimal stockPrice;

    @Column(name = "quantity")
    private int stockQuantity;

    public Transaction() {
    }

    public Transaction(final TransactionType transactionType, final String stockTicker, final BigDecimal stockPrice, final int stockQuantity) {
        this.transactionType = transactionType;
        this.stockTicker = stockTicker;
        this.stockPrice = stockPrice;
        this.stockQuantity = stockQuantity;
    }

    public static Transaction of(final TransactionDTO transactionDTO) {
        return new Transaction(transactionDTO.transactionType(), transactionDTO.stockTicker(), transactionDTO.stockPrice(), transactionDTO.stockQuantity());
    }
}