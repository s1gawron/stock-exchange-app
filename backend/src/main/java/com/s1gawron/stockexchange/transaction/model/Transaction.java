package com.s1gawron.stockexchange.transaction.model;

import com.s1gawron.stockexchange.transaction.dto.TransactionRequestDTO;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "public__transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "transaction_create_date", nullable = false)
    private LocalDateTime transactionCreateDate;

    @Column(name = "transaction_process_date")
    private LocalDateTime transactionProcessDate;

    @Column(name = "balance_blocked")
    private BigDecimal balanceBlocked;

    @Column(name = "balance_after_transaction")
    private BigDecimal balanceAfterTransaction;

    @Embedded
    private TransactionPosition transactionPosition;

    protected Transaction() {
    }

    public Transaction(final long walletId, final TransactionType transactionType, final TransactionStatus transactionStatus,
        final LocalDateTime transactionCreateDate, final TransactionPosition transactionPosition) {
        this.walletId = walletId;
        this.transactionType = transactionType;
        this.transactionStatus = transactionStatus;
        this.transactionCreateDate = transactionCreateDate;
        this.transactionPosition = transactionPosition;
    }

    public static Transaction createFrom(final long walletId, final TransactionRequestDTO transactionRequestDTO) {
        final TransactionPosition transactionPosition = TransactionPosition.createFrom(transactionRequestDTO);
        return new Transaction(walletId, transactionRequestDTO.type(), TransactionStatus.NEW, LocalDateTime.now(), transactionPosition);
    }

    public Long getId() {
        return id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public TransactionPosition getTransactionPosition() {
        return transactionPosition;
    }

    public LocalDateTime getTransactionProcessDate() {
        return transactionProcessDate;
    }

    public BigDecimal getBalanceBlocked() {
        return Optional.ofNullable(balanceBlocked).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceBlocked(final BigDecimal balanceBlocked) {
        this.balanceBlocked = balanceBlocked;
    }

    public void updateTransactionAfterProcessing(final BigDecimal balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.transactionStatus = TransactionStatus.COMPLETED;
        this.transactionProcessDate = LocalDateTime.now();
    }
}