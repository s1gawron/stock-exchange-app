package com.s1gawron.stockexchange.transaction.model;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", unique = true, nullable = false)
    private long transactionId;

    @Column(name = "buyer_id", nullable = false)
    private long buyerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Embedded
    private TransactionPosition transactionPosition;

    protected Transaction() {
    }

    public Transaction(final long buyerId, final TransactionType transactionType, final TransactionStatus transactionStatus,
        final LocalDateTime transactionDate,
        final TransactionPosition transactionPosition) {
        this.buyerId = buyerId;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.transactionDate = transactionDate;
        this.transactionPosition = transactionPosition;
    }

    public static Transaction createFrom(final long buyerId, final TransactionRequestDTO transactionRequestDTO) {
        final TransactionPosition transactionPosition = TransactionPosition.createFrom(transactionRequestDTO);
        return new Transaction(buyerId, transactionRequestDTO.transactionType(), TransactionStatus.NEW, LocalDateTime.now(), transactionPosition);
    }
}